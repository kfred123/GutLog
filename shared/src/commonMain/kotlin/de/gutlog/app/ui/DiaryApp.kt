package de.gutlog.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import de.gutlog.shared.generated.resources.Res
import de.gutlog.shared.generated.resources.app_name
import de.gutlog.shared.generated.resources.food_diary_description
import de.gutlog.shared.generated.resources.food_diary_title
import de.gutlog.shared.generated.resources.project_scaffold
import de.gutlog.shared.generated.resources.stool_log_description
import de.gutlog.shared.generated.resources.stool_log_title
import org.jetbrains.compose.resources.stringResource
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.gutlog.app.localization.AppLanguage
import de.gutlog.app.localization.LocalAppLocale
import de.gutlog.app.localization.rememberLanguagePreferenceStore
import de.gutlog.app.localization.resolveAppLanguage

@Composable
fun DiaryApp() {
    val preferences = rememberLanguagePreferenceStore()
    val currentDeviceLanguageTag = LocalAppLocale.current
    val deviceLanguageTag = remember { currentDeviceLanguageTag }
    var savedLanguageTag by remember { mutableStateOf(preferences.getLanguageTag()) }
    val selectedLanguage = resolveAppLanguage(savedLanguageTag, deviceLanguageTag)

    CompositionLocalProvider(LocalAppLocale provides selectedLanguage.tag) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6F8))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.app_name), fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
            Row {
                AppLanguage.entries.forEach { language ->
                    TextButton(
                        onClick = {
                            preferences.setLanguageTag(language.tag)
                            savedLanguageTag = language.tag
                        },
                    ) {
                        Text(
                            text = if (language == selectedLanguage) "✓ ${language.name}" else language.name,
                        )
                    }
                }
            }
            EntrySection(
                title = stringResource(Res.string.stool_log_title),
                description = stringResource(Res.string.stool_log_description),
            )
            EntrySection(
                title = stringResource(Res.string.food_diary_title),
                description = stringResource(Res.string.food_diary_description),
            )
            Text(stringResource(Res.string.project_scaffold))
        }
    }
}

@Composable
private fun EntrySection(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text(description)
    }
}
