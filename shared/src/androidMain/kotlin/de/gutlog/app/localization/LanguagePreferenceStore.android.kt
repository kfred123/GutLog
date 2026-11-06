package de.gutlog.app.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

private const val PREFERENCES_NAME = "gutlog_settings"
private const val LANGUAGE_TAG_KEY = "language_tag"

@Composable
actual fun rememberLanguagePreferenceStore(): LanguagePreferenceStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidLanguagePreferenceStore(context) }
}

actual fun deviceLanguageTag(): String = Locale.getDefault().toLanguageTag()

private class AndroidLanguagePreferenceStore(
    context: Context,
) : LanguagePreferenceStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getLanguageTag(): String? = preferences.getString(LANGUAGE_TAG_KEY, null)

    override fun setLanguageTag(languageTag: String?) {
        preferences.edit().apply {
            if (languageTag == null) {
                remove(LANGUAGE_TAG_KEY)
            } else {
                putString(LANGUAGE_TAG_KEY, languageTag)
            }
        }.apply()
    }
}
