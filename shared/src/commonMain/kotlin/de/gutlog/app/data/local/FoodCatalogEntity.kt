package de.gutlog.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.gutlog.app.domain.FoodCatalogItem
import de.gutlog.app.domain.FoodCategory

@Entity(tableName = "food_catalog")
data class FoodCatalogEntity(
    @PrimaryKey val id: String,
    val category: String?,
    val germanName: String?,
    val englishName: String?,
    val customName: String?,
) {
    fun toDomain() = FoodCatalogItem(
        id = id,
        category = category?.let(FoodCategory::valueOf),
        germanName = germanName,
        englishName = englishName,
        customName = customName,
    )

    companion object {
        fun fromDomain(item: FoodCatalogItem) = FoodCatalogEntity(
            id = item.id,
            category = item.category?.name,
            germanName = item.germanName,
            englishName = item.englishName,
            customName = item.customName,
        )
    }
}
