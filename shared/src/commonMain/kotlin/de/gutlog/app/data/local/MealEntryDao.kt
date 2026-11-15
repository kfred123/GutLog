package de.gutlog.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import de.gutlog.app.domain.MealFood
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {
    @Transaction
    @Query("SELECT * FROM meal_entries ORDER BY occurredAtEpochMillis DESC")
    fun observeAllWithFoods(): Flow<List<MealEntryWithFoods>>

    @Upsert
    suspend fun upsert(entry: MealEntryEntity)

    @Upsert
    suspend fun upsertFoods(entries: List<MealFoodEntity>)

    @Upsert
    suspend fun upsertCatalog(entries: List<FoodCatalogEntity>)

    @Query(
        "SELECT * FROM food_catalog WHERE :query = '' " +
            "OR germanName LIKE '%' || :query || '%' COLLATE NOCASE " +
            "OR englishName LIKE '%' || :query || '%' COLLATE NOCASE " +
            "OR customName LIKE '%' || :query || '%' COLLATE NOCASE " +
            "ORDER BY COALESCE(customName, englishName, germanName) COLLATE NOCASE",
    )
    suspend fun searchCatalog(query: String): List<FoodCatalogEntity>

    @Query("SELECT * FROM food_catalog WHERE customName = :name COLLATE NOCASE LIMIT 1")
    suspend fun findCustomFood(name: String): FoodCatalogEntity?

    @Transaction
    suspend fun saveMeal(entry: MealEntryEntity, foods: List<MealFood>) {
        val savedFoods = foods.map { food ->
            if (!food.isCustom) {
                food
            } else {
                val existing = findCustomFood(food.name.trim())
                val catalogItem = existing ?: FoodCatalogEntity(
                    id = "custom-${food.name.trim().lowercase()}",
                    category = null,
                    germanName = null,
                    englishName = null,
                    customName = food.name.trim(),
                ).also { upsertCatalog(listOf(it)) }
                food.copy(catalogFoodId = catalogItem.id)
            }
        }
        upsert(entry)
        upsertFoods(savedFoods.map { MealFoodEntity.fromDomain(it, entry.id) })
    }

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
