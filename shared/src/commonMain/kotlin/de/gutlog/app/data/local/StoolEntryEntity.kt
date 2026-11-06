package de.gutlog.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.gutlog.app.domain.StoolEntry

@Entity(tableName = "stool_entries")
data class StoolEntryEntity(
    @PrimaryKey val id: String,
    val occurredAtEpochMillis: Long,
    val bristolType: Int,
    val note: String?,
) {
    fun toDomain() = StoolEntry(
        id = id,
        occurredAtEpochMillis = occurredAtEpochMillis,
        bristolType = bristolType,
        note = note,
    )

    companion object {
        fun fromDomain(entry: StoolEntry) = StoolEntryEntity(
            id = entry.id,
            occurredAtEpochMillis = entry.occurredAtEpochMillis,
            bristolType = entry.bristolType,
            note = entry.note,
        )
    }
}
