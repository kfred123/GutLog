package de.gutlog.app.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults

private const val LANGUAGE_TAG_KEY = "gutlog.language_tag"

@Composable
actual fun rememberLanguagePreferenceStore(): LanguagePreferenceStore =
    remember { IosLanguagePreferenceStore() }

actual fun deviceLanguageTag(): String =
    NSLocale.preferredLanguages.firstOrNull() as? String ?: AppLanguage.English.tag

private class IosLanguagePreferenceStore : LanguagePreferenceStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getLanguageTag(): String? = defaults.stringForKey(LANGUAGE_TAG_KEY)

    override fun setLanguageTag(languageTag: String?) {
        if (languageTag == null) {
            defaults.removeObjectForKey(LANGUAGE_TAG_KEY)
        } else {
            defaults.setObject(languageTag, forKey = LANGUAGE_TAG_KEY)
        }
    }
}
