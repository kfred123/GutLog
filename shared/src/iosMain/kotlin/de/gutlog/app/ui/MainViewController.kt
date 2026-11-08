package de.gutlog.app.ui

import androidx.compose.ui.window.ComposeUIViewController
import de.gutlog.app.data.local.RoomStoolEntryRepository
import de.gutlog.app.data.local.buildDiaryDatabase

fun MainViewController(): platform.UIKit.UIViewController {
    val repository by lazy { RoomStoolEntryRepository(buildDiaryDatabase().stoolEntryDao()) }
    return ComposeUIViewController { DiaryApp { repository } }
}
