package de.gutlog.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import de.gutlog.app.domain.BuiltInFoodCatalog
import de.gutlog.app.domain.FoodCatalogItem
import de.gutlog.app.domain.MealEntry
import de.gutlog.app.domain.MealEntryRepository
import de.gutlog.app.domain.StoolEntry
import de.gutlog.app.domain.StoolEntryRepository
import de.gutlog.app.localization.LocalAppLocale
import de.gutlog.app.ui.MealLogState
import de.gutlog.app.ui.SharedBackHandler
import de.gutlog.app.ui.StoolLogScreen
import de.gutlog.app.ui.StoolLogState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class FoodDiaryUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private class StoolRepository : StoolEntryRepository {
        private val entries = MutableStateFlow(emptyList<StoolEntry>())
        override fun observeAll(): Flow<List<StoolEntry>> = entries
        override suspend fun lastSavedBristolType(): Int? = null
        override suspend fun save(entry: StoolEntry) { entries.value = listOf(entry) + entries.value }
        override suspend fun delete(id: String) = Unit
    }

    private class MealRepository : MealEntryRepository {
        val entries = MutableStateFlow(emptyList<MealEntry>())
        val customFoods = mutableListOf<FoodCatalogItem>()
        override fun observeAll(): Flow<List<MealEntry>> = entries
        override suspend fun searchFoods(query: String): List<FoodCatalogItem> =
            BuiltInFoodCatalog.search(query, customFoods)
        override suspend fun save(entry: MealEntry) {
            entry.foods.filter { it.isCustom }.forEach { food ->
                if (customFoods.none { it.customName.equals(food.name, ignoreCase = true) }) {
                    customFoods += FoodCatalogItem("custom-${food.name.lowercase()}", null, null, null, food.name)
                }
            }
            entries.value = listOf(entry) + entries.value
        }
        override suspend fun delete(id: String) = Unit
    }

    private fun show(repository: MealRepository, german: Boolean = false) {
        compose.setContent {
            val scope = rememberCoroutineScope()
            val stoolState = remember { StoolLogState(StoolRepository(), scope, { 0 }, { "stool" }) }
            val nextFoodId = remember { intArrayOf(0) }
            val mealState = remember {
                MealLogState(repository, scope, { 0 }, { "food-${++nextFoodId[0]}" })
            }
            val stoolDraft = stoolState.draft.collectAsState()
            val mealDraft = mealState.draft.collectAsState()
            SharedBackHandler(
                enabled = stoolDraft.value != null || mealDraft.value != null,
                onBack = { if (mealDraft.value != null) mealState.cancelDraft() else stoolState.cancelDraft() },
            )
            CompositionLocalProvider(LocalAppLocale provides if (german) "de" else "en") {
                StoolLogScreen(stoolState, german, mealState)
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun addsCatalogAndCustomFoodsWithMealAndFoodPortions() {
        show(MealRepository())
        compose.onNodeWithContentDescription("Add entry").performClick()
        compose.onNodeWithText("Add food").performClick()
        compose.onNodeWithText("Save").assertIsNotEnabled()
        compose.onNodeWithTag("food-search").performTextInput("tomato")
        compose.onNodeWithText("Tomato").performClick()
        compose.onNodeWithTag("food-search").performTextClearance()
        compose.onNodeWithTag("food-search").performTextInput("Oat milk")
        compose.onNodeWithText("Add as custom food: Oat milk").performClick()
        compose.onNodeWithText("Date (YYYY-MM-DD)").performTextClearance()
        compose.onNodeWithText("Date (YYYY-MM-DD)").performTextInput("1969-12-30")
        compose.onNodeWithText("Time (HH:MM)").performTextClearance()
        compose.onNodeWithText("Time (HH:MM)").performTextInput("12:00")
        compose.onNodeWithTag("meal-portion-LARGE").performClick()
        compose.onNodeWithTag("food-portion-food-1-SMALL").performClick()
        compose.onNodeWithText("Save").performClick()

        compose.onNodeWithTag("stool-log").performScrollToNode(hasText("Tomato — Small"))
        compose.onNodeWithText("Tomato — Small").assertIsDisplayed()
        compose.onNodeWithText("Oat milk — Large").assertIsDisplayed()
    }

    @Test
    fun cancellingCustomFoodDoesNotAddItToSearchCatalog() {
        val repository = MealRepository()
        show(repository)
        compose.onNodeWithContentDescription("Add entry").performClick()
        compose.onNodeWithText("Add food").performClick()
        compose.onNodeWithTag("food-search").performTextInput("My own food")
        compose.onNodeWithText("Add as custom food: My own food").performClick()
        compose.onNodeWithTag("cancel-meal").performClick()
        compose.onNodeWithContentDescription("Add entry").performClick()
        compose.onNodeWithText("Add food").performClick()
        compose.onNodeWithTag("food-search").performTextInput("My own food")
        compose.onNodeWithText("Add as custom food: My own food").assertIsDisplayed()
        assert(repository.entries.value.isEmpty())
        assert(repository.customFoods.isEmpty())
    }

    @Test
    fun germanUiFindsEnglishCatalogNames() {
        show(MealRepository(), german = true)
        compose.onNodeWithContentDescription("Eintrag hinzufügen").performClick()
        compose.onNodeWithText("Essen eintragen").performClick()
        compose.onNodeWithText("Lebensmittel suchen").assertIsDisplayed()
        compose.onNodeWithTag("food-search").performTextInput("apple")
        compose.onNodeWithText("Apfel").performClick()
        compose.onNodeWithText("Portionsgröße der Mahlzeit").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("meal-portion-LARGE").assertIsDisplayed()
    }
}
