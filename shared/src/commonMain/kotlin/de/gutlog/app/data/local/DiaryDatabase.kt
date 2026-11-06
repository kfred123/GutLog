package de.gutlog.app.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [StoolEntryEntity::class, MealEntryEntity::class],
    version = 1,
)
@ConstructedBy(DiaryDatabaseConstructor::class)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun stoolEntryDao(): StoolEntryDao

    abstract fun mealEntryDao(): MealEntryDao
}

@Suppress("KotlinNoActualForExpect")
expect object DiaryDatabaseConstructor : RoomDatabaseConstructor<DiaryDatabase> {
    override fun initialize(): DiaryDatabase
}
