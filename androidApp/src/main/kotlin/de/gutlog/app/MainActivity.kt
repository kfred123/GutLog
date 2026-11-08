package de.gutlog.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.gutlog.app.data.local.buildStoolEntryRepository
import de.gutlog.app.ui.DiaryApp

class MainActivity : ComponentActivity() {
    private val repository by lazy {
        buildStoolEntryRepository(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiaryApp { repository }
        }
    }
}
