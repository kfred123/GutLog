package de.gutlog.app.domain

data class MealEntry(
    val id: String,
    val occurredAtEpochMillis: Long,
    val description: String,
    val note: String? = null,
) {
    init {
        require(id.isNotBlank()) { "An entry ID is required." }
        require(description.isNotBlank()) { "A meal description is required." }
    }
}
