package de.gutlog.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {
    @Query("SELECT * FROM meal_entries ORDER BY occurredAtEpochMillis DESC")
    fun observeAll(): Flow<List<MealEntryEntity>>

    @Upsert
    suspend fun upsert(entry: MealEntryEntity)

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
