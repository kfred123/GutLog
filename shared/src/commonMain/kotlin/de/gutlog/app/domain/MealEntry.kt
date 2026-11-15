package de.gutlog.app.domain

data class MealEntry(
    val id: String,
    val occurredAtEpochMillis: Long,
    val description: String,
    val note: String? = null,
    val portionSize: PortionSize = PortionSize.MEDIUM,
    val foods: List<MealFood> = listOf(
        MealFood(id = "$id-food-0", name = description),
    ),
) {
    init {
        require(id.isNotBlank()) { "An entry ID is required." }
        require(description.isNotBlank()) { "A meal description is required." }
        require(foods.isNotEmpty()) { "A meal must contain at least one food." }
        require(foods.all { it.name.isNotBlank() }) { "Food names are required." }
    }
}

enum class PortionSize {
    SMALL,
    MEDIUM,
    LARGE,
}

data class MealFood(
    val id: String,
    val name: String,
    val catalogFoodId: String? = null,
    val portionOverride: PortionSize? = null,
    val isCustom: Boolean = false,
) {
    init {
        require(id.isNotBlank()) { "A food ID is required." }
        require(name.isNotBlank()) { "A food name is required." }
    }

    fun effectivePortion(mealPortion: PortionSize): PortionSize =
        portionOverride ?: mealPortion
}
