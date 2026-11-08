package de.gutlog.app.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class DiaryEntryTest {
    @Test
    fun stoolEntryAcceptsBristolScaleBounds() {
        assertEquals(1, StoolEntry("first", 0, 1).bristolType)
        assertEquals(7, StoolEntry("last", 0, 7).bristolType)
    }

    @Test
    fun stoolEntryRejectsInvalidBristolScale() {
        for (type in listOf(0, 8)) {
            assertFailsWith<IllegalArgumentException> {
                StoolEntry("invalid", 0, type)
            }
        }
    }

    @Test
    fun stoolFlagsDefaultToFalse() {
        val entry = StoolEntry("default", 0, 4)
        assertFalse(entry.hasBlood)
        assertFalse(entry.isUrgent)
    }

    @Test
    fun stoolFlagsAreIndependent() {
        for (blood in listOf(false, true)) {
            for (urgent in listOf(false, true)) {
                val entry = StoolEntry("flags", 0, 4, "note", blood, urgent)
                assertEquals(blood, entry.hasBlood)
                assertEquals(urgent, entry.isUrgent)
                assertEquals("note", entry.note)
            }
        }
    }

    @Test
    fun mealEntryRejectsBlankDescription() {
        assertFailsWith<IllegalArgumentException> {
            MealEntry("invalid", 0, "  ")
        }
    }
}
