package de.gutlog.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import de.gutlog.app.domain.MealEntry
import de.gutlog.app.domain.MealFood
import de.gutlog.app.domain.PortionSize

@Entity(tableName = "meal_entries")
data class MealEntryEntity(
    @PrimaryKey val id: String,
    val occurredAtEpochMillis: Long,
    val description: String,
    val note: String?,
    @ColumnInfo(defaultValue = "'MEDIUM'") val portionSize: String = PortionSize.MEDIUM.name,
) {
    companion object {
        fun fromDomain(entry: MealEntry) = MealEntryEntity(
            id = entry.id,
            occurredAtEpochMillis = entry.occurredAtEpochMillis,
            description = entry.description,
            note = entry.note,
            portionSize = entry.portionSize.name,
        )
    }
}

data class MealEntryWithFoods(
    @Embedded val meal: MealEntryEntity,
    @Relation(parentColumn = "id", entityColumn = "mealEntryId")
    val foods: List<MealFoodEntity>,
) {
    fun toDomain(): MealEntry {
        val mealFoods = if (foods.isEmpty()) {
            listOf(MealFood(id = "${meal.id}-legacy-food", name = meal.description))
        } else {
            foods.map(MealFoodEntity::toDomain)
        }
        return MealEntry(
            id = meal.id,
            occurredAtEpochMillis = meal.occurredAtEpochMillis,
            description = meal.description,
            note = meal.note,
            portionSize = PortionSize.valueOf(meal.portionSize),
            foods = mealFoods,
        )
    }
}
