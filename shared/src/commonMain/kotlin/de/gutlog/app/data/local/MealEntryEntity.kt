package de.gutlog.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.gutlog.app.domain.MealEntry

@Entity(tableName = "meal_entries")
data class MealEntryEntity(
    @PrimaryKey val id: String,
    val occurredAtEpochMillis: Long,
    val description: String,
    val note: String?,
) {
    fun toDomain() = MealEntry(
        id = id,
        occurredAtEpochMillis = occurredAtEpochMillis,
        description = description,
        note = note,
    )

    companion object {
        fun fromDomain(entry: MealEntry) = MealEntryEntity(
            id = entry.id,
            occurredAtEpochMillis = entry.occurredAtEpochMillis,
            description = entry.description,
            note = entry.note,
        )
    }
}
