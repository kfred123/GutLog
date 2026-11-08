package de.gutlog.app.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun buildDiaryDatabase(): DiaryDatabase =
    Room.databaseBuilder<DiaryDatabase>(name = diaryDatabasePath())
        .addMigrations(DIARY_MIGRATION_1_2)
        .setDriver(BundledSQLiteDriver())
        .build()
