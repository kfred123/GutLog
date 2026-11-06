package de.gutlog.app.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DiaryEntryTest {
    @Test
    fun stoolEntryAcceptsBristolScaleBounds() {
        assertEquals(1, StoolEntry("first", 0, 1).bristolType)
        assertEquals(7, StoolEntry("last", 0, 7).bristolType)
    }

    @Test
    fun stoolEntryRejectsInvalidBristolScale() {
        assertFailsWith<IllegalArgumentException> {
            StoolEntry("invalid", 0, 8)
        }
    }

    @Test
    fun mealEntryRejectsBlankDescription() {
        assertFailsWith<IllegalArgumentException> {
            MealEntry("invalid", 0, "  ")
        }
    }
}
