package de.gutlog.app.ui

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class EntryTimeTest {
    private val zone = TimeZone.of("Europe/Berlin")
    private val now = Instant.parse("2025-03-30T12:00:00Z").toEpochMilliseconds()

    @Test
    fun convertsPastAndCurrentTimesAndRejectsFuture() {
        assertEquals(
            Instant.parse("2025-03-29T23:15:00Z").toEpochMilliseconds(),
            resolveEntryTime(LocalDateTime(2025, 3, 30, 0, 15), zone, now).epochMillis,
        )
        assertEquals(now, resolveEntryTime(LocalDateTime(2025, 3, 30, 14, 0), zone, now).epochMillis)
        assertEquals(EntryTimeError.FUTURE, resolveEntryTime(LocalDateTime(2025, 3, 30, 14, 1), zone, now).error)
        assertEquals("30.03.2025 00:15", formatEntryTime(Instant.parse("2025-03-29T23:15:00Z").toEpochMilliseconds(), zone, true))
        assertEquals("03/30/2025 00:15", formatEntryTime(Instant.parse("2025-03-29T23:15:00Z").toEpochMilliseconds(), zone, false))
    }

    @Test
    fun rejectsDaylightSavingGapsAndOverlaps() {
        assertEquals(EntryTimeError.INVALID_OR_AMBIGUOUS, resolveEntryTime(LocalDateTime(2025, 3, 30, 2, 30), zone, now).error)
        val later = Instant.parse("2025-10-27T00:00:00Z").toEpochMilliseconds()
        assertEquals(EntryTimeError.INVALID_OR_AMBIGUOUS, resolveEntryTime(LocalDateTime(2025, 10, 26, 2, 30), zone, later).error)
    }
}
