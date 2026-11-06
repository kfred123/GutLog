package de.gutlog.app.data.local

import de.gutlog.app.domain.StoolEntry
import de.gutlog.app.domain.StoolEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomStoolEntryRepository(
    private val dao: StoolEntryDao,
) : StoolEntryRepository {
    override fun observeAll(): Flow<List<StoolEntry>> =
        dao.observeAll().map { entries -> entries.map(StoolEntryEntity::toDomain) }

    override suspend fun save(entry: StoolEntry) {
        dao.upsert(StoolEntryEntity.fromDomain(entry))
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }
}
