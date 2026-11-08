package de.gutlog.app.domain

import kotlinx.coroutines.flow.Flow

interface StoolEntryRepository {
    fun observeAll(): Flow<List<StoolEntry>>

    suspend fun lastSavedBristolType(): Int?

    suspend fun save(entry: StoolEntry)

    suspend fun delete(id: String)
}
