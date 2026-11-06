package de.gutlog.app.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults

private const val APPLE_LANGUAGES_KEY = "AppleLanguages"

actual object LocalAppLocale {
    private val locale = staticCompositionLocalOf { deviceLanguageTag() }

    actual val current: String
        @Composable get() = locale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val selected = value ?: deviceLanguageTag()
        if (value == null) {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(APPLE_LANGUAGES_KEY)
        } else {
            NSUserDefaults.standardUserDefaults.setObject(arrayListOf(selected), forKey = APPLE_LANGUAGES_KEY)
        }
        return locale.provides(selected)
    }
}

private fun deviceLanguageTag(): String =
    NSLocale.preferredLanguages.firstOrNull() as? String ?: AppLanguage.English.tag
