package de.gutlog.app.localization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppLanguageTest {
    @Test
    fun resolvesSupportedDeviceLocales() {
        assertEquals(AppLanguage.German, resolveAppLanguage(null, "de-DE"))
        assertEquals(AppLanguage.English, resolveAppLanguage(null, "en-GB"))
    }

    @Test
    fun fallsBackToEnglishForUnsupportedDeviceLocales() {
        assertEquals(AppLanguage.English, resolveAppLanguage(null, "fr-FR"))
    }

    @Test
    fun savedLanguageOverridesDeviceLocale() {
        assertEquals(AppLanguage.English, resolveAppLanguage("en", "de-DE"))
        assertEquals(AppLanguage.German, resolveAppLanguage("de", "en-US"))
    }

    @Test
    fun ignoresUnsupportedSavedLanguage() {
        assertEquals(AppLanguage.German, resolveAppLanguage("fr", "de-DE"))
        assertNull(appLanguageForTag("fr-FR"))
    }
}
