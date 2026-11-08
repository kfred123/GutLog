package de.gutlog.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import de.gutlog.app.domain.StoolEntry

@Entity(tableName = "stool_entries")
data class StoolEntryEntity(
    @PrimaryKey val id: String,
    val occurredAtEpochMillis: Long,
    val bristolType: Int,
    val note: String?,
    @ColumnInfo(defaultValue = "0") val hasBlood: Boolean,
    @ColumnInfo(defaultValue = "0") val isUrgent: Boolean,
) {
    fun toDomain() = StoolEntry(
        id = id,
        occurredAtEpochMillis = occurredAtEpochMillis,
        bristolType = bristolType,
        note = note,
        hasBlood = hasBlood,
        isUrgent = isUrgent,
    )

    companion object {
        fun fromDomain(entry: StoolEntry) = StoolEntryEntity(
            id = entry.id,
            occurredAtEpochMillis = entry.occurredAtEpochMillis,
            bristolType = entry.bristolType,
            note = entry.note,
            hasBlood = entry.hasBlood,
            isUrgent = entry.isUrgent,
        )
    }
}
