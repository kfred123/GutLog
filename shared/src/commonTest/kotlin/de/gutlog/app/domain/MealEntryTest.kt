package de.gutlog.app.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MealEntryTest {
    @Test
    fun foodsInheritMealPortionUnlessOverridden() {
        val inherited = MealFood("inherited", "Apple")
        val overridden = MealFood("overridden", "Bread", portionOverride = PortionSize.SMALL)

        assertEquals(PortionSize.LARGE, inherited.effectivePortion(PortionSize.LARGE))
        assertEquals(PortionSize.SMALL, overridden.effectivePortion(PortionSize.LARGE))
        assertEquals(PortionSize.MEDIUM, MealEntry("meal", 0, "Apple").portionSize)
    }

    @Test
    fun mealCanContainMultipleFoodsWithIndependentPortions() {
        val meal = MealEntry(
            id = "meal",
            occurredAtEpochMillis = 123,
            description = "Apple, bread",
            portionSize = PortionSize.MEDIUM,
            foods = listOf(
                MealFood("apple", "Apple"),
                MealFood("bread", "Bread", portionOverride = PortionSize.LARGE),
            ),
        )

        assertEquals(2, meal.foods.size)
        assertEquals(PortionSize.MEDIUM, meal.foods[0].effectivePortion(meal.portionSize))
        assertEquals(PortionSize.LARGE, meal.foods[1].effectivePortion(meal.portionSize))
    }

    @Test
    fun mealRequiresAtLeastOneFoodAndNonBlankFoodNames() {
        assertFailsWith<IllegalArgumentException> {
            MealEntry("empty", 0, "empty", foods = emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            MealEntry("blank", 0, "blank", foods = listOf(MealFood("food", "  ")))
        }
    }

    @Test
    fun builtInCatalogCoversRequiredCategoriesAndSearchesBothLanguages() {
        assertEquals(
            setOf(FoodCategory.VEGETABLE, FoodCategory.FRUIT, FoodCategory.MEAT, FoodCategory.LEGUME),
            BuiltInFoodCatalog.items.mapNotNull { it.category }.toSet(),
        )
        val tomato = BuiltInFoodCatalog.search("Tomate").single()
        assertEquals("Tomato", tomato.displayName(german = false))
        assertEquals("Tomate", tomato.displayName(german = true))
        assertEquals("Apple", BuiltInFoodCatalog.search("apple").single().displayName(german = false))
    }

    @Test
    fun customFoodSearchAndReuseIgnoreCase() {
        val custom = FoodCatalogItem(
            id = "custom-oat-milk",
            category = null,
            germanName = null,
            englishName = null,
            customName = "Oat Milk",
        )

        assertTrue(BuiltInFoodCatalog.search("oat", listOf(custom)).contains(custom))
        assertEquals("Oat Milk", custom.displayName(german = true))
        assertEquals(custom, BuiltInFoodCatalog.findOrReuseCustom("  oat milk ", listOf(custom)))
        assertFalse(BuiltInFoodCatalog.search("missing").any { it.id == custom.id })
    }
}
