package de.gutlog.app.ui

import de.gutlog.app.domain.FoodCatalogItem
import de.gutlog.app.domain.MealEntry
import de.gutlog.app.domain.MealEntryRepository
import de.gutlog.app.domain.MealFood
import de.gutlog.app.domain.PortionSize
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

sealed interface MealLogContent {
    data object Loading : MealLogContent
    data object Error : MealLogContent
    data class Loaded(val entries: List<MealEntry>) : MealLogContent
}

data class MealDraft(
    val localDateTime: LocalDateTime? = null,
    val portionSize: PortionSize = PortionSize.MEDIUM,
    val foods: List<MealFood> = emptyList(),
)

class MealLogState(
    private val repository: MealEntryRepository,
    private val scope: CoroutineScope,
    private val now: () -> Long,
    private val newId: () -> String,
    private val zone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) {
    private val mutableContent = MutableStateFlow<MealLogContent>(MealLogContent.Loading)
    val content: StateFlow<MealLogContent> = mutableContent.asStateFlow()
    private val mutableDraft = MutableStateFlow<MealDraft?>(null)
    val draft: StateFlow<MealDraft?> = mutableDraft.asStateFlow()
    private val mutableFoodSearchOpen = MutableStateFlow(false)
    val foodSearchOpen: StateFlow<Boolean> = mutableFoodSearchOpen.asStateFlow()
    private val mutableSearchResults = MutableStateFlow<List<FoodCatalogItem>>(emptyList())
    val searchResults: StateFlow<List<FoodCatalogItem>> = mutableSearchResults.asStateFlow()
    private val mutableSearching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = mutableSearching.asStateFlow()
    private val mutableSaving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = mutableSaving.asStateFlow()
    private val mutableSaveError = MutableStateFlow(false)
    val saveError: StateFlow<Boolean> = mutableSaveError.asStateFlow()
    private val mutableTimeError = MutableStateFlow<EntryTimeError?>(null)
    val timeError: StateFlow<EntryTimeError?> = mutableTimeError.asStateFlow()
    private val mutableSearchError = MutableStateFlow(false)
    val searchError: StateFlow<Boolean> = mutableSearchError.asStateFlow()
    private var observation: Job? = null
    private var searchJob: Job? = null
    private var searchGeneration = 0

    init {
        reload()
    }

    fun reload() {
        observation?.cancel()
        mutableContent.value = MealLogContent.Loading
        observation = scope.launch {
            try {
                repository.observeAll().collect { entries ->
                    mutableContent.value = MealLogContent.Loaded(entries)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableContent.value = MealLogContent.Error
            }
        }
    }

    fun openDraft() {
        if (content.value is MealLogContent.Loaded && draft.value == null) {
            mutableDraft.value = MealDraft(
                localDateTime = Instant.fromEpochMilliseconds(now()).toLocalDateTime(zone()),
            )
            mutableSaveError.value = false
            mutableTimeError.value = null
            mutableSearchError.value = false
            mutableSearchResults.value = emptyList()
            searchFoods("")
        }
    }

    fun openFoodSearch() {
        if (draft.value == null || saving.value || foodSearchOpen.value) return
        mutableFoodSearchOpen.value = true
        searchFoods("")
    }

    fun closeFoodSearch() {
        mutableFoodSearchOpen.value = false
    }

    fun searchFoods(query: String) {
        searchJob?.cancel()
        val generation = ++searchGeneration
        mutableSearchError.value = false
        mutableSearchResults.value = emptyList()
        mutableSearching.value = true
        searchJob = scope.launch {
            try {
                val results = repository.searchFoods(query)
                if (generation == searchGeneration) mutableSearchResults.value = results
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (generation == searchGeneration) {
                    mutableSearchResults.value = emptyList()
                    mutableSearchError.value = true
                }
            } finally {
                if (generation == searchGeneration) mutableSearching.value = false
            }
        }
    }

    fun addFood(item: FoodCatalogItem, german: Boolean) {
        if (saving.value || draft.value == null) return
        mutableDraft.value = requireNotNull(draft.value).copy(
            foods = draft.value!!.foods + MealFood(
                id = newId(),
                name = item.displayName(german),
                catalogFoodId = item.id,
                isCustom = item.isCustom,
            ),
        )
        mutableSaveError.value = false
        closeFoodSearch()
    }

    fun addCustomFood(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || saving.value || draft.value == null) return
        mutableDraft.value = requireNotNull(draft.value).copy(
            foods = draft.value!!.foods + MealFood(
                id = newId(),
                name = trimmed,
                isCustom = true,
            ),
        )
        mutableSaveError.value = false
        closeFoodSearch()
    }

    fun removeFood(id: String) {
        if (!saving.value) {
            mutableDraft.value = draft.value?.let { current ->
                current.copy(foods = current.foods.filterNot { it.id == id })
            }
        }
    }

    fun setPortionSize(size: PortionSize) {
        if (!saving.value) mutableDraft.value = draft.value?.copy(portionSize = size)
    }

    fun setFoodPortion(id: String, size: PortionSize?) {
        if (!saving.value) {
            mutableDraft.value = draft.value?.let { current ->
                current.copy(
                    foods = current.foods.map { food ->
                        if (food.id == id) food.copy(portionOverride = size) else food
                    },
                )
            }
        }
    }

    fun setDateTime(value: LocalDateTime) {
        if (!saving.value) {
            mutableDraft.value = draft.value?.copy(localDateTime = value)
            mutableTimeError.value = null
        }
    }

    fun cancelDraft() {
        if (!saving.value) {
            closeFoodSearch()
            mutableDraft.value = null
            mutableSaveError.value = false
            mutableTimeError.value = null
            mutableSearchResults.value = emptyList()
        }
    }

    fun saveDraft() {
        val current = draft.value ?: return
        if (saving.value || current.foods.isEmpty()) return
        val local = current.localDateTime ?: return
        val time = resolveEntryTime(local, zone(), now())
        if (time.error != null) {
            mutableTimeError.value = time.error
            return
        }
        val id = newId()
        val entry = MealEntry(
            id = id,
            occurredAtEpochMillis = requireNotNull(time.epochMillis),
            description = current.foods.joinToString(", ") { it.name },
            portionSize = current.portionSize,
            foods = current.foods,
        )
        mutableSaving.value = true
        mutableSaveError.value = false
        scope.launch {
            try {
                repository.save(entry)
                closeFoodSearch()
                mutableDraft.value = null
                mutableSearchResults.value = emptyList()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableSaveError.value = true
            } finally {
                mutableSaving.value = false
            }
        }
    }

    fun delete(id: String) {
        scope.launch {
            try {
                repository.delete(id)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableContent.value = MealLogContent.Error
            }
        }
    }
}
