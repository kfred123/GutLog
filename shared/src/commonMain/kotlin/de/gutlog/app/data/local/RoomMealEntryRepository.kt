package de.gutlog.app.data.local

import de.gutlog.app.domain.MealEntry
import de.gutlog.app.domain.MealEntryRepository
import de.gutlog.app.domain.BuiltInFoodCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMealEntryRepository(
    private val dao: MealEntryDao,
) : MealEntryRepository {
    override fun observeAll(): Flow<List<MealEntry>> =
        dao.observeAllWithFoods().map { entries -> entries.map(MealEntryWithFoods::toDomain) }

    override suspend fun searchFoods(query: String): List<de.gutlog.app.domain.FoodCatalogItem> {
        dao.upsertCatalog(BuiltInFoodCatalog.items.map(FoodCatalogEntity::fromDomain))
        return dao.searchCatalog("").map(FoodCatalogEntity::toDomain).filter { it.matches(query) }
    }

    override suspend fun save(entry: MealEntry) {
        dao.saveMeal(MealEntryEntity.fromDomain(entry), entry.foods)
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }
}
