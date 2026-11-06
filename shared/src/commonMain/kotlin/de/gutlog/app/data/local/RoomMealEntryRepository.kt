package de.gutlog.app.data.local

import de.gutlog.app.domain.MealEntry
import de.gutlog.app.domain.MealEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMealEntryRepository(
    private val dao: MealEntryDao,
) : MealEntryRepository {
    override fun observeAll(): Flow<List<MealEntry>> =
        dao.observeAll().map { entries -> entries.map(MealEntryEntity::toDomain) }

    override suspend fun save(entry: MealEntry) {
        dao.upsert(MealEntryEntity.fromDomain(entry))
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }
}
