package de.gutlog.app.ui

import de.gutlog.app.domain.BuiltInFoodCatalog
import de.gutlog.app.domain.FoodCatalogItem
import de.gutlog.app.domain.MealEntry
import de.gutlog.app.domain.MealEntryRepository
import de.gutlog.app.domain.PortionSize
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MealLogStateTest {
    private class FakeRepository : MealEntryRepository {
        val entries = MutableStateFlow<List<MealEntry>>(emptyList())
        val customFoods = mutableListOf<FoodCatalogItem>()
        var saved: MealEntry? = null
        var failSave = false
        var failSearch = false
        var saveGate: CompletableDeferred<Unit>? = null

        override fun observeAll(): Flow<List<MealEntry>> = entries

        override suspend fun searchFoods(query: String): List<FoodCatalogItem> {
            if (failSearch) error("search failed")
            return BuiltInFoodCatalog.search(query, customFoods)
        }

        override suspend fun save(entry: MealEntry) {
            saveGate?.await()
            if (failSave) error("save failed")
            saved = entry
            customFoods += entry.foods.filter { it.isCustom }.map {
                FoodCatalogItem("custom-${it.name.lowercase()}", null, null, null, it.name)
            }
            entries.value = listOf(entry) + entries.value
        }

        override suspend fun delete(id: String) {
            entries.value = entries.value.filterNot { it.id == id }
        }
    }

    @Test
    fun openingFoodSearchResetsResultsAndErrorsAndIsUnavailableWithoutDraftOrWhileSaving() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = FakeRepository()
            val state = MealLogState(repository, scope, { 0 }, { "id" }, { TimeZone.UTC })
            state.openFoodSearch()
            assertFalse(state.foodSearchOpen.value)

            state.openDraft()
            state.openFoodSearch()
            assertTrue(state.foodSearchOpen.value)
            assertEquals(BuiltInFoodCatalog.search("", emptyList()), state.searchResults.value)
            state.searchFoods("tomato")
            assertEquals(1, state.searchResults.value.size)
            state.closeFoodSearch()
            repository.failSearch = true
            state.openFoodSearch()
            assertTrue(state.searchError.value)
            state.closeFoodSearch()
            repository.failSearch = false
            state.openFoodSearch()
            assertFalse(state.searchError.value)
            assertEquals(BuiltInFoodCatalog.search("", emptyList()), state.searchResults.value)

            state.closeFoodSearch()
            state.addCustomFood("Apple")
            repository.saveGate = CompletableDeferred()
            state.saveDraft()
            assertTrue(state.saving.value)
            state.openFoodSearch()
            assertFalse(state.foodSearchOpen.value)
            repository.saveGate?.complete(Unit)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun foodSearchClosesAfterAddingOrDiscardingWithoutChangingTheDraft() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            var nextId = 0
            val state = MealLogState(FakeRepository(), scope, { 0 }, { "id-${nextId++}" }, { TimeZone.UTC })
            state.openDraft()
            val original = state.draft.value
            state.openFoodSearch()
            state.closeFoodSearch()
            assertEquals(original, state.draft.value)

            state.openFoodSearch()
            state.addFood(state.searchResults.value.first(), german = false)
            assertFalse(state.foodSearchOpen.value)
            assertEquals(1, state.draft.value?.foods?.size)

            state.openFoodSearch()
            state.addCustomFood(" Oat milk ")
            assertFalse(state.foodSearchOpen.value)
            assertEquals(2, state.draft.value?.foods?.size)
            assertEquals("Oat milk", state.draft.value?.foods?.last()?.name)

            state.openFoodSearch()
            state.cancelDraft()
            assertFalse(state.foodSearchOpen.value)
            assertNull(state.draft.value)

            state.openDraft()
            state.addCustomFood("Apple")
            state.openFoodSearch()
            state.saveDraft()
            assertFalse(state.foodSearchOpen.value)
            assertNull(state.draft.value)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun opensDraftAddsFoodsAndSavesWithIndependentPortions() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = FakeRepository()
            val ids = generateSequence(1) { it + 1 }.map { "id-$it" }.iterator()
            val state = MealLogState(repository, scope, { 0 }, { ids.next() }, { TimeZone.UTC })
            assertEquals(MealLogContent.Loaded(emptyList()), state.content.value)

            state.openDraft()
            assertEquals(LocalDateTime(1970, 1, 1, 0, 0), state.draft.value?.localDateTime)
            assertTrue(state.searchResults.value.isNotEmpty())
            state.searchFoods("tomate")
            assertEquals("Tomate", state.searchResults.value.single().displayName(german = true))
            state.addFood(state.searchResults.value.single(), german = true)
            state.addCustomFood("Oat milk")
            val foods = requireNotNull(state.draft.value).foods
            state.setDateTime(LocalDateTime(1969, 12, 31, 23, 59))
            state.setPortionSize(PortionSize.LARGE)
            state.setFoodPortion(foods.first().id, PortionSize.SMALL)
            state.saveDraft()

            val saved = requireNotNull(repository.saved)
            assertEquals(-60_000L, saved.occurredAtEpochMillis)
            assertEquals(PortionSize.LARGE, saved.portionSize)
            assertEquals(PortionSize.SMALL, saved.foods.first().portionOverride)
            assertTrue(saved.foods.last().isCustom)
            assertNull(state.draft.value)
            assertEquals(MealLogContent.Loaded(listOf(saved)), state.content.value)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun cancelAndFailedSaveDoNotPersistDraft() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = FakeRepository()
            repository.failSave = true
            val state = MealLogState(repository, scope, { 0 }, { "id" }, { TimeZone.UTC })
            state.openDraft()
            state.addCustomFood("  Lentils ")
            state.saveDraft()
            assertTrue(state.saveError.value)
            assertTrue(repository.entries.value.isEmpty())
            assertTrue(requireNotNull(state.draft.value).foods.single().isCustom)
            state.cancelDraft()
            assertNull(state.draft.value)
            assertTrue(repository.customFoods.isEmpty())
            repository.failSave = false
            state.openDraft()
            state.addCustomFood("Lentils")
            state.saveDraft()
            assertEquals("Lentils", repository.saved?.foods?.single()?.name)
            assertEquals(1, repository.customFoods.size)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun removingFoodAndChangingTimeUpdatesDraft() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            var nextId = 0
            val state = MealLogState(FakeRepository(), scope, { 0 }, { "id-${nextId++}" }, { TimeZone.UTC })
            state.openDraft()
            state.addCustomFood("Apple")
            state.addCustomFood("Bread")
            val first = requireNotNull(state.draft.value).foods.first()
            state.setDateTime(LocalDateTime(1969, 12, 31, 23, 59))
            state.removeFood(first.id)

            assertEquals(LocalDateTime(1969, 12, 31, 23, 59), state.draft.value?.localDateTime)
            assertEquals(listOf("Bread"), state.draft.value?.foods?.map { it.name })
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun cannotSaveEmptyMealAndRejectsFutureTime() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = FakeRepository()
            val state = MealLogState(repository, scope, { 0 }, { "id" }, { TimeZone.UTC })
            state.openDraft()
            state.saveDraft()
            assertNull(repository.saved)

            state.addCustomFood("Apple")
            state.setDateTime(LocalDateTime(1970, 1, 1, 0, 1))
            state.saveDraft()
            assertEquals(EntryTimeError.FUTURE, state.timeError.value)
            assertNull(repository.saved)
            assertFalse(state.saving.value)
        } finally {
            scope.cancel()
        }
    }
}
