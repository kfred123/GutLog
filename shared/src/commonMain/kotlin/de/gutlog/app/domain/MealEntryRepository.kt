package de.gutlog.app.domain

import kotlinx.coroutines.flow.Flow

interface MealEntryRepository {
    fun observeAll(): Flow<List<MealEntry>>

    suspend fun save(entry: MealEntry)

    suspend fun delete(id: String)
}
