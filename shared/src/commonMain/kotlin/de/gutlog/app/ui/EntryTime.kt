package de.gutlog.app.ui

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

enum class EntryTimeError { INVALID_OR_AMBIGUOUS, FUTURE }

data class EntryTimeResult(val epochMillis: Long? = null, val error: EntryTimeError? = null)

fun resolveEntryTime(local: LocalDateTime, zone: TimeZone, nowMillis: Long): EntryTimeResult {
    val approximate = local.toInstant(zone)
    val naive = local.toInstant(TimeZone.UTC)
    val offsets = listOf(
        zone.offsetAt(approximate - 2.days),
        zone.offsetAt(approximate),
        zone.offsetAt(approximate + 2.days),
    ).distinct()
    val candidates = offsets.map { naive - it.totalSeconds.seconds }
        .filter { it.toLocalDateTime(zone) == local }
        .distinct()
    if (candidates.size != 1) return EntryTimeResult(error = EntryTimeError.INVALID_OR_AMBIGUOUS)
    val epoch = candidates.single().toEpochMilliseconds()
    if (epoch > nowMillis) return EntryTimeResult(error = EntryTimeError.FUTURE)
    return EntryTimeResult(epochMillis = epoch)
}

fun formatEntryTime(epochMillis: Long, zone: TimeZone, german: Boolean): String {
    val local = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
    fun Int.twoDigits() = toString().padStart(2, '0')
    val date = if (german) {
        "${local.day.twoDigits()}.${local.monthNumber.twoDigits()}.${local.year}"
    } else {
        "${local.monthNumber.twoDigits()}/${local.day.twoDigits()}/${local.year}"
    }
    return "$date ${local.hour.twoDigits()}:${local.minute.twoDigits()}"
}
