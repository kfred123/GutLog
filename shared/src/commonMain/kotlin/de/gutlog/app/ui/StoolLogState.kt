package de.gutlog.app.ui

import de.gutlog.app.domain.StoolEntry
import de.gutlog.app.domain.StoolEntryRepository
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Instant

sealed interface StoolLogContent {
    data object Loading : StoolLogContent
    data object Error : StoolLogContent
    data class Loaded(val entries: List<StoolEntry>) : StoolLogContent
}

data class StoolDraft(
    val bristolType: Int? = null,
    val hasBlood: Boolean = false,
    val isUrgent: Boolean = false,
    val localDateTime: LocalDateTime? = null,
)

class StoolLogState(
    private val repository: StoolEntryRepository,
    private val scope: CoroutineScope,
    private val now: () -> Long,
    private val newId: () -> String,
    private val zone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) {
    private val mutableContent = MutableStateFlow<StoolLogContent>(StoolLogContent.Loading)
    val content: StateFlow<StoolLogContent> = mutableContent.asStateFlow()
    private val mutableDraft = MutableStateFlow<StoolDraft?>(null)
    val draft: StateFlow<StoolDraft?> = mutableDraft.asStateFlow()
    private val mutableSaving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = mutableSaving.asStateFlow()
    private val mutableSaveError = MutableStateFlow(false)
    val saveError: StateFlow<Boolean> = mutableSaveError.asStateFlow()
    private val mutableTimeError = MutableStateFlow<EntryTimeError?>(null)
    val timeError: StateFlow<EntryTimeError?> = mutableTimeError.asStateFlow()
    private var draftId: String? = null
    private var lastSavedType: Int? = null
    private var observation: Job? = null

    init {
        reload()
    }

    fun reload() {
        observation?.cancel()
        mutableContent.value = StoolLogContent.Loading
        observation = scope.launch {
            try {
                lastSavedType = repository.lastSavedBristolType()
                repository.observeAll().collect { entries ->
                    mutableContent.value = StoolLogContent.Loaded(entries)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableContent.value = StoolLogContent.Error
            }
        }
    }

    fun openDraft() {
        if (content.value is StoolLogContent.Loaded && draft.value == null) {
            mutableDraft.value = StoolDraft(
                bristolType = lastSavedType,
                localDateTime = Instant.fromEpochMilliseconds(now()).toLocalDateTime(zone()),
            )
            draftId = null
            mutableSaveError.value = false
            mutableTimeError.value = null
        }
    }

    fun selectType(type: Int) {
        require(type in 1..7)
        if (!saving.value) mutableDraft.value = mutableDraft.value?.copy(bristolType = type)
    }

    fun setBlood(value: Boolean) {
        if (!saving.value) mutableDraft.value = mutableDraft.value?.copy(hasBlood = value)
    }

    fun setUrgent(value: Boolean) {
        if (!saving.value) mutableDraft.value = mutableDraft.value?.copy(isUrgent = value)
    }

    fun setDateTime(value: LocalDateTime) {
        if (!saving.value) {
            mutableDraft.value = mutableDraft.value?.copy(localDateTime = value)
            mutableTimeError.value = null
        }
    }

    fun cancelDraft() {
        if (!saving.value) {
            mutableDraft.value = null
            draftId = null
            mutableSaveError.value = false
            mutableTimeError.value = null
        }
    }

    fun saveDraft() {
        val current = draft.value ?: return
        val type = current.bristolType ?: return
        if (saving.value) return
        val local = current.localDateTime ?: return
        val time = resolveEntryTime(local, zone(), now())
        if (time.error != null) {
            mutableTimeError.value = time.error
            return
        }
        val id = draftId ?: newId().also { draftId = it }
        val entry = StoolEntry(
            id = id,
            occurredAtEpochMillis = requireNotNull(time.epochMillis),
            bristolType = type,
            hasBlood = current.hasBlood,
            isUrgent = current.isUrgent,
        )
        mutableSaving.value = true
        mutableSaveError.value = false
        scope.launch {
            try {
                repository.save(entry)
                lastSavedType = type
                mutableDraft.value = null
                draftId = null
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableSaveError.value = true
            } finally {
                mutableSaving.value = false
            }
        }
    }
}
