package de.gutlog.app.localization

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

actual object LocalAppLocale {
    private var deviceLocale: Locale? = null

    actual val current: String
        @Composable get() {
            val configuration = LocalConfiguration.current
            if (deviceLocale == null) {
                deviceLocale = configuration.locales[0] ?: Locale.getDefault()
            }
            return configuration.locales[0]?.toLanguageTag() ?: Locale.getDefault().toLanguageTag()
        }

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val currentConfiguration = LocalConfiguration.current
        if (deviceLocale == null) {
            deviceLocale = currentConfiguration.locales[0] ?: Locale.getDefault()
        }
        val locale = value?.let(Locale::forLanguageTag) ?: deviceLocale!!
        Locale.setDefault(locale)
        val configuration = Configuration(currentConfiguration).apply {
            setLocale(locale)
        }
        return LocalConfiguration.provides(configuration)
    }
}
