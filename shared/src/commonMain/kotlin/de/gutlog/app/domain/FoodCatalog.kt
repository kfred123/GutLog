package de.gutlog.app.domain

enum class FoodCategory {
    VEGETABLE,
    FRUIT,
    MEAT,
    LEGUME,
}

data class FoodCatalogItem(
    val id: String,
    val category: FoodCategory?,
    val germanName: String?,
    val englishName: String?,
    val customName: String? = null,
) {
    init {
        require(id.isNotBlank()) { "A catalog food ID is required." }
        require(!germanName.isNullOrBlank() || !englishName.isNullOrBlank() || !customName.isNullOrBlank()) {
            "A catalog food name is required."
        }
    }

    val isCustom: Boolean get() = customName != null

    fun displayName(german: Boolean): String =
        customName ?: if (german) germanName ?: requireNotNull(englishName) else englishName ?: requireNotNull(germanName)

    fun matches(query: String): Boolean {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return true
        return listOfNotNull(germanName, englishName, customName)
            .any { normalized in it.lowercase() }
    }

    fun matchesExactly(query: String): Boolean {
        val normalized = query.trim()
        if (normalized.isEmpty()) return false
        return listOfNotNull(germanName, englishName, customName)
            .any { it.equals(normalized, ignoreCase = true) }
    }
}

object BuiltInFoodCatalog {
    val items: List<FoodCatalogItem> = listOf(
        vegetable("tomato", "Tomate", "Tomato"),
        vegetable("cucumber", "Gurke", "Cucumber"),
        vegetable("carrot", "Karotte", "Carrot"),
        vegetable("broccoli", "Brokkoli", "Broccoli"),
        vegetable("spinach", "Spinat", "Spinach"),
        vegetable("bell-pepper", "Paprika", "Bell pepper"),
        vegetable("onion", "Zwiebel", "Onion"),
        vegetable("potato", "Kartoffel", "Potato"),
        vegetable("zucchini", "Zucchini", "Zucchini"),
        vegetable("lettuce", "Salat", "Lettuce"),
        vegetable("cauliflower", "Blumenkohl", "Cauliflower"),
        vegetable("mushroom", "Champignon", "Mushroom"),
        fruit("apple", "Apfel", "Apple"),
        fruit("banana", "Banane", "Banana"),
        fruit("orange", "Orange", "Orange"),
        fruit("strawberry", "Erdbeere", "Strawberry"),
        fruit("grape", "Weintraube", "Grape"),
        fruit("pear", "Birne", "Pear"),
        fruit("blueberry", "Heidelbeere", "Blueberry"),
        fruit("peach", "Pfirsich", "Peach"),
        fruit("avocado", "Avocado", "Avocado"),
        fruit("lemon", "Zitrone", "Lemon"),
        meat("chicken", "Hähnchen", "Chicken"),
        meat("beef", "Rindfleisch", "Beef"),
        meat("pork", "Schweinefleisch", "Pork"),
        meat("turkey", "Pute", "Turkey"),
        meat("salmon", "Lachs", "Salmon"),
        meat("tuna", "Thunfisch", "Tuna"),
        meat("egg", "Ei", "Egg"),
        legume("lentil", "Linse", "Lentil"),
        legume("chickpea", "Kichererbse", "Chickpea"),
        legume("kidney-bean", "Kidneybohne", "Kidney bean"),
        legume("black-bean", "Schwarze Bohne", "Black bean"),
        legume("pea", "Erbse", "Pea"),
        legume("soybean", "Sojabohne", "Soybean"),
    )

    fun search(query: String, customItems: List<FoodCatalogItem> = emptyList()): List<FoodCatalogItem> =
        (items + customItems).filter { it.matches(query) }

    fun findOrReuseCustom(name: String, customItems: List<FoodCatalogItem>): FoodCatalogItem? {
        val normalized = name.trim()
        if (normalized.isEmpty()) return null
        return customItems.firstOrNull {
            it.isCustom && it.customName?.equals(normalized, ignoreCase = true) == true
        }
    }

    private fun vegetable(id: String, german: String, english: String) =
        builtIn(id, FoodCategory.VEGETABLE, german, english)

    private fun fruit(id: String, german: String, english: String) =
        builtIn(id, FoodCategory.FRUIT, german, english)

    private fun meat(id: String, german: String, english: String) =
        builtIn(id, FoodCategory.MEAT, german, english)

    private fun legume(id: String, german: String, english: String) =
        builtIn(id, FoodCategory.LEGUME, german, english)

    private fun builtIn(id: String, category: FoodCategory, german: String, english: String) =
        FoodCatalogItem("builtin-$id", category, german, english)
}
