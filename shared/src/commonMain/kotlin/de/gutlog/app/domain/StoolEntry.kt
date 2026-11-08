package de.gutlog.app.domain

data class StoolEntry(
    val id: String,
    val occurredAtEpochMillis: Long,
    val bristolType: Int,
    val note: String? = null,
    val hasBlood: Boolean = false,
    val isUrgent: Boolean = false,
) {
    init {
        require(id.isNotBlank()) { "An entry ID is required." }
        require(bristolType in 1..7) { "Bristol scale values must be between 1 and 7." }
    }
}
