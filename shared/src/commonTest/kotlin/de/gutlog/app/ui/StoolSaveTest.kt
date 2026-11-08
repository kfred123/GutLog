package de.gutlog.app.ui

import de.gutlog.app.domain.StoolEntry
import de.gutlog.app.domain.StoolEntryRepository
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StoolSaveTest {
    private class FakeRepository : StoolEntryRepository {
        val entries = MutableStateFlow<List<StoolEntry>>(emptyList())
        val attempted = mutableListOf<StoolEntry>()
        var savedType: Int? = 3
        var fail = false
        var gate: CompletableDeferred<Unit>? = null
        override fun observeAll() = entries
        override suspend fun lastSavedBristolType() = savedType
        override suspend fun save(entry: StoolEntry) {
            attempted.add(entry)
            gate?.await()
            if (fail) throw IllegalStateException("save failed")
            savedType = entry.bristolType
        }
        override suspend fun delete(id: String) = Unit
    }

    @Test
    fun duplicateSaveAndDiscardAreBlockedUntilSuccess() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = FakeRepository().apply { gate = CompletableDeferred() }
            val state = StoolLogState(repository, scope, { 10 }, { "fixed" })
            state.openDraft()
            state.selectType(5)
            state.saveDraft()
            state.saveDraft()
            state.cancelDraft()
            assertTrue(state.saving.value)
            assertEquals(1, repository.attempted.size)
            assertEquals(5, state.draft.value?.bristolType)
            repository.gate!!.complete(Unit)
            assertFalse(state.saving.value)
            assertNull(state.draft.value)
            state.openDraft()
            assertEquals(5, state.draft.value?.bristolType)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun failureKeepsDraftAndRetryUsesSameIdentity() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = FakeRepository().apply { fail = true }
            val state = StoolLogState(repository, scope, { 10 }, { "fixed" })
            state.openDraft()
            state.selectType(7)
            state.setBlood(true)
            state.saveDraft()
            assertTrue(state.saveError.value)
            assertEquals(7, state.draft.value?.bristolType)
            assertEquals(3, repository.savedType)
            repository.fail = false
            state.saveDraft()
            assertFalse(state.saveError.value)
            assertNull(state.draft.value)
            assertEquals(listOf("fixed", "fixed"), repository.attempted.map { it.id })
            state.openDraft()
            assertEquals(7, state.draft.value?.bristolType)
            assertFalse(state.draft.value!!.hasBlood)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun futureTimeDoesNotSave() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = FakeRepository()
            val state = StoolLogState(repository, scope, { 0 }, { "fixed" }, { TimeZone.UTC })
            state.openDraft()
            state.setDateTime(LocalDateTime(1970, 1, 1, 0, 1))
            state.saveDraft()
            assertEquals(EntryTimeError.FUTURE, state.timeError.value)
            assertTrue(repository.attempted.isEmpty())
        } finally {
            scope.cancel()
        }
    }
}
