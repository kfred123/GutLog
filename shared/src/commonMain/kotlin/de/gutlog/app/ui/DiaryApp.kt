package de.gutlog.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.gutlog.app.domain.StoolEntryRepository
import de.gutlog.app.localization.AppLanguage
import de.gutlog.app.localization.LocalAppLocale
import de.gutlog.app.localization.rememberLanguagePreferenceStore
import de.gutlog.app.localization.resolveAppLanguage
import de.gutlog.shared.generated.resources.Res
import de.gutlog.shared.generated.resources.app_name
import de.gutlog.shared.generated.resources.load_error
import de.gutlog.shared.generated.resources.retry
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun DiaryApp(repositoryFactory: () -> StoolEntryRepository) {
    val preferences = rememberLanguagePreferenceStore()
    val currentDeviceLanguageTag = LocalAppLocale.current
    val deviceLanguageTag = remember { currentDeviceLanguageTag }
    var savedLanguageTag by remember { mutableStateOf(preferences.getLanguageTag()) }
    val selectedLanguage = resolveAppLanguage(savedLanguageTag, deviceLanguageTag)
    var repositoryResult by remember { mutableStateOf(runCatching(repositoryFactory)) }
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalAppLocale provides selectedLanguage.tag) {
        Column(
            Modifier.fillMaxSize().background(Color(0xFFF5F6F8))
                .windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(Res.string.app_name), fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
            Row {
                AppLanguage.entries.forEach { language ->
                    TextButton(onClick = {
                        preferences.setLanguageTag(language.tag)
                        savedLanguageTag = language.tag
                    }) {
                        Text(if (language == selectedLanguage) "✓ ${language.name}" else language.name)
                    }
                }
            }
            val repository = repositoryResult.getOrNull()
            if (repository == null) {
                Text(stringResource(Res.string.load_error))
                TextButton(onClick = { repositoryResult = runCatching(repositoryFactory) }) {
                    Text(stringResource(Res.string.retry))
                }
            } else {
                val state = remember(repository) {
                    StoolLogState(
                        repository,
                        scope,
                        now = { Clock.System.now().toEpochMilliseconds() },
                        newId = { Uuid.random().toString() },
                    )
                }
                val draft by state.draft.collectAsState()
                SharedBackHandler(enabled = draft != null, onBack = state::cancelDraft)
                StoolLogScreen(state, selectedLanguage == AppLanguage.German)
            }
        }
    }
}
