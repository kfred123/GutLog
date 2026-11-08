package de.gutlog.app.ui

import de.gutlog.app.domain.StoolEntry
import de.gutlog.app.domain.StoolEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlin.test.Test
import kotlin.test.assertEquals

class StoolLogStateTest {
    private class FakeRepository : StoolEntryRepository {
        val entries = MutableStateFlow<List<StoolEntry>>(emptyList())
        var fail = false
        var savedType: Int? = null
        override fun observeAll(): Flow<List<StoolEntry>> =
            if (fail) flow { throw IllegalStateException("read failed") } else entries
        override suspend fun lastSavedBristolType(): Int? {
            if (fail) throw IllegalStateException("read failed")
            return savedType
        }
        override suspend fun save(entry: StoolEntry) = Unit
        override suspend fun delete(id: String) = Unit
    }

    @Test
    fun observesUpdatesAndRetriesErrors() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = FakeRepository()
            val state = StoolLogState(repository, scope, { 0 }, { "id" })
            assertEquals(StoolLogContent.Loaded(emptyList()), state.content.value)
            val entry = StoolEntry("id", 1, 4)
            repository.entries.value = listOf(entry)
            assertEquals(StoolLogContent.Loaded(listOf(entry)), state.content.value)
            repository.fail = true
            state.reload()
            assertEquals(StoolLogContent.Error, state.content.value)
            repository.fail = false
            state.reload()
            assertEquals(StoolLogContent.Loaded(listOf(entry)), state.content.value)
        } finally {
            scope.cancel()
        }
    }

        @Test
        fun draftsUseSavedDefaultAndKeepChangesIndependentOfLogUpdates() {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
            try {
                val repository = FakeRepository()
                val state = StoolLogState(repository, scope, { 0 }, { "id" })
                state.openDraft()
                assertEquals(StoolDraft(), state.draft.value?.copy(localDateTime = null))
                for (type in 1..7) {
                    state.selectType(type)
                    assertEquals(type, state.draft.value?.bristolType)
                }
                for (blood in listOf(false, true)) {
                    for (urgent in listOf(false, true)) {
                        state.setBlood(blood)
                        state.setUrgent(urgent)
                        assertEquals(StoolDraft(7, blood, urgent), state.draft.value?.copy(localDateTime = null))
                    }
                }
                repository.entries.value = listOf(StoolEntry("id", 1, 2))
                assertEquals(StoolDraft(7, true, true), state.draft.value?.copy(localDateTime = null))
                state.cancelDraft()
                assertEquals(emptyList(), repository.entries.value.filter { it.id != "id" })

                repository.savedType = 3
                state.reload()
                state.openDraft()
                assertEquals(StoolDraft(3), state.draft.value?.copy(localDateTime = null))
                state.selectType(5)
                state.cancelDraft()
                state.openDraft()
                assertEquals(StoolDraft(3), state.draft.value?.copy(localDateTime = null))
            } finally {
                scope.cancel()
            }
        }

        @Test
        fun readFailurePreventsOpeningDraft() {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
            try {
                val repository = FakeRepository().apply { fail = true }
                val state = StoolLogState(repository, scope, { 0 }, { "id" })
                state.openDraft()
                assertEquals(null, state.draft.value)
                assertEquals(StoolLogContent.Error, state.content.value)
            } finally {
                scope.cancel()
            }
        }

        @Test
        fun loadingPreventsOpeningAndRestartUsesSavedTypeNotEventOrder() {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
            try {
                val updates = MutableSharedFlow<List<StoolEntry>>(replay = 1)
                val repository = object : StoolEntryRepository {
                    override fun observeAll(): Flow<List<StoolEntry>> = updates
                    override suspend fun lastSavedBristolType(): Int = 5
                    override suspend fun save(entry: StoolEntry) = Unit
                    override suspend fun delete(id: String) = Unit
                }
                val state = StoolLogState(repository, scope, { 0 }, { "id" })
                state.openDraft()
                assertEquals(null, state.draft.value)
                assertEquals(StoolLogContent.Loading, state.content.value)
                updates.tryEmit(listOf(StoolEntry("newer", 100, 3), StoolEntry("last-saved", 1, 5)))
                state.openDraft()
                assertEquals(StoolDraft(5), state.draft.value?.copy(localDateTime = null))
            } finally {
                scope.cancel()
            }
    }
}
