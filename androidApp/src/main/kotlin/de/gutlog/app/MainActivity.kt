package de.gutlog.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.gutlog.app.data.local.buildDiaryRepositories
import de.gutlog.app.ui.DiaryApp

class MainActivity : ComponentActivity() {
    private val repositories by lazy {
        buildDiaryRepositories(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiaryApp({ repositories.stoolEntries }, { repositories.meals })
        }
    }
}
