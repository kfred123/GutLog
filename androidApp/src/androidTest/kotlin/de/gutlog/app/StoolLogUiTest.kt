package de.gutlog.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import de.gutlog.app.domain.StoolEntry
import de.gutlog.app.domain.StoolEntryRepository
import de.gutlog.app.localization.LocalAppLocale
import de.gutlog.app.ui.StoolLogScreen
import de.gutlog.app.ui.StoolLogState
import de.gutlog.app.ui.SharedBackHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import org.junit.Test

class StoolLogUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private class FakeRepository : StoolEntryRepository {
        val entries = MutableStateFlow<List<StoolEntry>>(emptyList())
        var type: Int? = null
        var failSave = false
        var failRead = false
        var waiting: MutableSharedFlow<List<StoolEntry>>? = null
        override fun observeAll(): Flow<List<StoolEntry>> =
            if (failRead) flow { throw IllegalStateException("read failed") } else waiting ?: entries
        override suspend fun lastSavedBristolType() = type
        override suspend fun save(entry: StoolEntry) {
            if (failSave) throw IllegalStateException("save failed")
            type = entry.bristolType
            entries.value = listOf(entry) + entries.value
        }
        override suspend fun delete(id: String) = Unit
    }

    private fun show(repository: FakeRepository) {
        compose.setContent {
            val scope = rememberCoroutineScope()
            val state = androidx.compose.runtime.remember {
                StoolLogState(repository, scope, { 1735689600000 }, { "test-entry" })
            }
            val draft = state.draft.collectAsState()
            SharedBackHandler(enabled = draft.value != null, onBack = state::cancelDraft)
            CompositionLocalProvider(LocalAppLocale provides "en") {
                StoolLogScreen(state, german = false)
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun menuAndStoolEntryFlow() {
        show(FakeRepository())
        compose.onNodeWithText("No bowel movements recorded yet.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Add entry").performClick()
        compose.onNodeWithText("Add food — Coming soon").assertIsNotEnabled()
        compose.onNodeWithText("Add pain — Coming soon").assertIsNotEnabled()
        compose.onNodeWithText("Add bowel movement").performClick()
        compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)).assertCountEquals(7)
        compose.onNodeWithText("Save").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("2: Lumpy sausage").performScrollTo().performClick()
        compose.onNodeWithText("2: Lumpy sausage").assertIsSelected()
        compose.onNodeWithText("4: Smooth, soft sausage").performScrollTo().performClick()
        compose.onNodeWithText("4: Smooth, soft sausage").assertIsSelected()
        compose.onNodeWithText("Blood").performScrollTo().performClick()
        compose.onNodeWithText("Urgent").performScrollTo().performClick()
        compose.onNodeWithText("Save").performScrollTo().performClick()
        compose.onNodeWithText("Blood").assertIsDisplayed()
        compose.onNodeWithText("Urgent").assertIsDisplayed()
        compose.onNodeWithContentDescription("Add entry").performClick()
        compose.onNodeWithText("Add bowel movement").performClick()
        compose.onNodeWithText("4: Smooth, soft sausage").performScrollTo().assertIsSelected()
        compose.onNodeWithText("Blood").performScrollTo().assertIsOff()
        compose.onNodeWithText("Urgent").performScrollTo().assertIsOff()
        compose.onNodeWithText("Cancel").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Add entry").assertIsDisplayed()
    }

    @Test
    fun rejectsFutureTimeAndKeepsDraftOnSaveFailure() {
        val repository = FakeRepository().apply { failSave = true }
        show(repository)
        compose.onNodeWithContentDescription("Add entry").performClick()
        compose.onNodeWithText("Add bowel movement").performClick()
        compose.onNodeWithText("Date (YYYY-MM-DD)").performTextClearance()
        compose.onNodeWithText("Date (YYYY-MM-DD)").performTextInput("2099-01-01")
        compose.onNodeWithText("2: Lumpy sausage").performScrollTo().performClick()
        compose.onNodeWithText("Save").performScrollTo().performClick()
        compose.onNodeWithText("The date and time cannot be in the future.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Date (YYYY-MM-DD)").performScrollTo().performTextClearance()
        compose.onNodeWithText("Date (YYYY-MM-DD)").performTextInput("2024-01-01")
        compose.onNodeWithText("Blood").performScrollTo().performClick()
        compose.onNodeWithText("Blood").assertIsOn()
        compose.onNodeWithText("Urgent").performScrollTo().assertIsOff()
        compose.onNodeWithText("Save").performScrollTo().performClick()
        compose.onNodeWithText("Could not save the entry. Please try again.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("2: Lumpy sausage").performScrollTo().assertIsSelected()
        compose.onNodeWithText("Cancel").performScrollTo().performClick()
        compose.onNodeWithText("No bowel movements recorded yet.").assertIsDisplayed()
    }

    @Test
    fun showsLoading() {
        val repository = FakeRepository().apply { waiting = MutableSharedFlow() }
        show(repository)
        compose.onNodeWithText("Loading entries…").assertIsDisplayed()
    }

    @Test
    fun readFailureCanBeRetried() {
        val repository = FakeRepository().apply { failRead = true }
        show(repository)
        compose.onNodeWithText("Could not load entries.").assertIsDisplayed()
        compose.runOnIdle { repository.failRead = false }
        compose.onNodeWithText("Retry").performClick()
        compose.onNodeWithText("No bowel movements recorded yet.").assertIsDisplayed()
    }

    @Test
    fun addButtonRemainsVisibleAfterScrolling() {
        val repository = FakeRepository()
        repository.entries.value = (1..30).map { StoolEntry("$it", 1735689600000 - it, 4) }
        show(repository)
        compose.onNodeWithTag("stool-log").performScrollToNode(hasText("Food diary"))
        compose.onNodeWithContentDescription("Add entry").assertIsDisplayed()
    }

    @Test
    fun logVisualizesBristolShapeBloodAndUrgency() {
        val repository = FakeRepository().apply {
            entries.value = listOf(
                StoolEntry("bloody-urgent", 1735689600000, 4, hasBlood = true, isUrgent = true),
                StoolEntry("normal", 1735689500000, 2),
            )
        }
        show(repository)

        compose.onNodeWithTag("stool-shape-bloody-urgent").assertIsDisplayed()
        compose.onNodeWithContentDescription("Bristol type 4 stool shape, blood marked").assertIsDisplayed()
        compose.onNodeWithTag("urgent-icon-bloody-urgent").assertIsDisplayed()
        compose.onNodeWithText("Blood").assertIsDisplayed()
        compose.onNodeWithText("Urgent").assertIsDisplayed()
        compose.onNodeWithTag("stool-shape-normal").assertIsDisplayed()
        compose.onNodeWithContentDescription("Bristol type 2 stool shape").assertIsDisplayed()
        compose.onAllNodes(hasTestTag("urgent-icon-normal")).assertCountEquals(0)
    }

    @Test
    fun dismissingMenuDoesNotOpenForm() {
        show(FakeRepository())
        compose.onNodeWithContentDescription("Add entry").performClick()
        compose.onNodeWithTag("stool-log").performTouchInput { click(Offset(10f, 10f)) }
        compose.onNodeWithText("No bowel movements recorded yet.").assertIsDisplayed()
        compose.onNodeWithText("Save").assertDoesNotExist()
    }

    @Test
    fun systemBackDiscardsDraft() {
        show(FakeRepository())
        compose.onNodeWithContentDescription("Add entry").performClick()
        compose.onNodeWithText("Add bowel movement").performClick()
        compose.onNodeWithText("3: Sausage with cracks").performScrollTo().performClick()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("No bowel movements recorded yet.").assertIsDisplayed()
        compose.onNodeWithText("Save").assertDoesNotExist()
    }
}
