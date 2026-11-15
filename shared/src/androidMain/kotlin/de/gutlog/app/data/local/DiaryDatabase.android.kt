package de.gutlog.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import de.gutlog.app.domain.MealEntryRepository
import de.gutlog.app.domain.StoolEntryRepository

fun buildDiaryDatabase(context: Context): DiaryDatabase {
    val appContext = context.applicationContext
    return Room.databaseBuilder<DiaryDatabase>(
        context = appContext,
        name = diaryDatabasePath(appContext),
    )
        .addMigrations(DIARY_MIGRATION_1_2)
        .setDriver(BundledSQLiteDriver())
        .build()
}

fun buildStoolEntryRepository(context: Context): RoomStoolEntryRepository =
    RoomStoolEntryRepository(buildDiaryDatabase(context).stoolEntryDao())

data class DiaryRepositories(
    val stoolEntries: StoolEntryRepository,
    val meals: MealEntryRepository,
)

fun buildDiaryRepositories(context: Context): DiaryRepositories {
    val database = buildDiaryDatabase(context)
    return DiaryRepositories(
        stoolEntries = RoomStoolEntryRepository(database.stoolEntryDao()),
        meals = RoomMealEntryRepository(database.mealEntryDao()),
    )
}
