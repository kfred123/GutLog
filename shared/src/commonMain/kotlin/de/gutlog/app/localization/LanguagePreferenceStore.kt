package de.gutlog.app.localization

import androidx.compose.runtime.Composable

interface LanguagePreferenceStore {
    fun getLanguageTag(): String?

    fun setLanguageTag(languageTag: String?)
}

@Composable
expect fun rememberLanguagePreferenceStore(): LanguagePreferenceStore

expect fun deviceLanguageTag(): String
