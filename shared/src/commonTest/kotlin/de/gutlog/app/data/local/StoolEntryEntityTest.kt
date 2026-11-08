package de.gutlog.app.data.local

import de.gutlog.app.domain.StoolEntry
import kotlin.test.Test
import kotlin.test.assertEquals

class StoolEntryEntityTest {
    @Test
    fun allFlagCombinationsRoundTrip() {
        for (blood in listOf(false, true)) {
            for (urgent in listOf(false, true)) {
                val entry = StoolEntry(
                    id = "entry",
                    occurredAtEpochMillis = 123456789,
                    bristolType = 7,
                    note = "note",
                    hasBlood = blood,
                    isUrgent = urgent,
                )
                assertEquals(entry, StoolEntryEntity.fromDomain(entry).toDomain())
            }
        }
    }
}
