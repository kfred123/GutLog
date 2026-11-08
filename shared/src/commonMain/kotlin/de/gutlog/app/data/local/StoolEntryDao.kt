package de.gutlog.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StoolEntryDao {
    @Query("SELECT * FROM stool_entries ORDER BY occurredAtEpochMillis DESC")
    fun observeAll(): Flow<List<StoolEntryEntity>>

    @Upsert
    suspend fun upsert(entry: StoolEntryEntity)

    @Query("SELECT bristolType FROM stool_default WHERE id = 1")
    suspend fun lastSavedBristolType(): Int?

    @Upsert
    suspend fun upsertDefault(value: StoolDefaultEntity)

    @Transaction
    suspend fun saveWithDefault(entry: StoolEntryEntity) {
        upsert(entry)
        upsertDefault(StoolDefaultEntity(bristolType = entry.bristolType))
    }

    @Query("DELETE FROM stool_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
