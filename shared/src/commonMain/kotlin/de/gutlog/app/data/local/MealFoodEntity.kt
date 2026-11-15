package de.gutlog.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.gutlog.app.domain.MealFood
import de.gutlog.app.domain.PortionSize

@Entity(
    tableName = "meal_foods",
    foreignKeys = [
        ForeignKey(
            entity = MealEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealEntryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("mealEntryId")],
)
data class MealFoodEntity(
    @PrimaryKey val id: String,
    val mealEntryId: String,
    val name: String,
    val catalogFoodId: String?,
    val portionOverride: String?,
    val isCustom: Boolean,
) {
    fun toDomain() = MealFood(
        id = id,
        name = name,
        catalogFoodId = catalogFoodId,
        portionOverride = portionOverride?.let(PortionSize::valueOf),
        isCustom = isCustom,
    )

    companion object {
        fun fromDomain(food: MealFood, mealEntryId: String) = MealFoodEntity(
            id = food.id,
            mealEntryId = mealEntryId,
            name = food.name,
            catalogFoodId = food.catalogFoodId,
            portionOverride = food.portionOverride?.name,
            isCustom = food.isCustom,
        )
    }
}
