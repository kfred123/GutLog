package de.gutlog.app.localization

enum class AppLanguage(val tag: String) {
    English("en"),
    German("de"),
}

fun appLanguageForTag(tag: String?): AppLanguage? =
    when (tag?.replace('_', '-')?.substringBefore('-')?.lowercase()) {
        "en" -> AppLanguage.English
        "de" -> AppLanguage.German
        else -> null
    }

fun resolveAppLanguage(
    savedLanguageTag: String?,
    deviceLanguageTag: String,
): AppLanguage =
    appLanguageForTag(savedLanguageTag)
        ?: appLanguageForTag(deviceLanguageTag)
        ?: AppLanguage.English
