package de.gutlog.app

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.gutlog.app.data.local.DIARY_MIGRATION_1_2
import de.gutlog.app.data.local.DiaryDatabase
import de.gutlog.app.data.local.RoomMealEntryRepository
import de.gutlog.app.data.local.RoomStoolEntryRepository
import de.gutlog.app.domain.FoodCategory
import de.gutlog.app.domain.MealEntry
import de.gutlog.app.domain.MealFood
import de.gutlog.app.domain.PortionSize
import de.gutlog.app.domain.StoolEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiaryDatabaseMigrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "migration-test.db"

    @After
    fun cleanup() {
        context.deleteDatabase(name)
    }

    private fun open(): DiaryDatabase =
        Room.databaseBuilder<DiaryDatabase>(context, context.getDatabasePath(name).absolutePath)
            .addMigrations(DIARY_MIGRATION_1_2)
            .setDriver(BundledSQLiteDriver())
            .build()

    private inline fun DiaryDatabase.use(block: (DiaryDatabase) -> Unit) {
        try {
            block(this)
        } finally {
            close()
        }
    }

    private fun legacyDatabase() {
        val path = context.getDatabasePath(name)
        path.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { db ->
            db.execSQL("CREATE TABLE stool_entries (id TEXT NOT NULL PRIMARY KEY, occurredAtEpochMillis INTEGER NOT NULL, bristolType INTEGER NOT NULL, note TEXT)")
            db.execSQL("CREATE TABLE meal_entries (id TEXT NOT NULL PRIMARY KEY, occurredAtEpochMillis INTEGER NOT NULL, description TEXT NOT NULL, note TEXT)")
            db.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            db.execSQL("INSERT INTO room_master_table VALUES (42, '88425de0952d2b04651dc818934a7a56')")
            db.execSQL("INSERT INTO stool_entries VALUES ('a', 100, 2, 'old note'), ('b', 200, 3, NULL), ('c', 200, 4, NULL)")
            db.execSQL("INSERT INTO meal_entries VALUES ('meal', 99, 'food', 'meal note')")
            db.version = 1
        }
    }

    private fun versionTwoDatabase() {
        val path = context.getDatabasePath(name)
        path.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { db ->
            db.execSQL("CREATE TABLE stool_entries (id TEXT NOT NULL PRIMARY KEY, occurredAtEpochMillis INTEGER NOT NULL, bristolType INTEGER NOT NULL, note TEXT, hasBlood INTEGER NOT NULL DEFAULT 0, isUrgent INTEGER NOT NULL DEFAULT 0)")
            db.execSQL("CREATE TABLE meal_entries (id TEXT NOT NULL PRIMARY KEY, occurredAtEpochMillis INTEGER NOT NULL, description TEXT NOT NULL, note TEXT)")
            db.execSQL("CREATE TABLE stool_default (id INTEGER NOT NULL PRIMARY KEY, bristolType INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            db.execSQL("INSERT INTO room_master_table VALUES (42, 'aa88a23a53bfc79735501fdae83f250c')")
            db.execSQL("INSERT INTO meal_entries VALUES ('meal', 99, 'rice and beans', 'lunch note')")
            db.version = 2
        }
    }

    @Test
    fun migrationPreservesDataAndBackdatedDefault() = runBlocking {
        legacyDatabase()
        open().use { database ->
            val repository = RoomStoolEntryRepository(database.stoolEntryDao())
            val entries = repository.observeAll().first()
            assertEquals(3, entries.size)
            assertEquals("old note", entries.single { it.id == "a" }.note)
            assertTrue(entries.all { !it.hasBlood && !it.isUrgent })
            assertEquals(4, repository.lastSavedBristolType())
            val meal = RoomMealEntryRepository(database.mealEntryDao()).observeAll().first().single()
            assertEquals("food", meal.description)
            assertEquals("meal note", meal.note)
            repository.save(StoolEntry("backdated", 1, 5, "new", true, true))
        }
        open().use { database ->
            val repository = RoomStoolEntryRepository(database.stoolEntryDao())
            assertEquals(5, repository.lastSavedBristolType())
            assertEquals(StoolEntry("backdated", 1, 5, "new", true, true), repository.observeAll().first().last())
        }
    }

    @Test
    fun migrationFromVersionTwoPreservesLegacyMealAndUsesMediumDefault() = runBlocking {
        versionTwoDatabase()
        open().use { database ->
            val meals = RoomMealEntryRepository(database.mealEntryDao()).observeAll().first()
            val meal = meals.single()
            assertEquals(99L, meal.occurredAtEpochMillis)
            assertEquals("rice and beans", meal.description)
            assertEquals("lunch note", meal.note)
            assertEquals(PortionSize.MEDIUM, meal.portionSize)
            assertEquals(listOf("rice and beans"), meal.foods.map { it.name })
            assertEquals(null, meal.foods.single().portionOverride)
        }
    }

    @Test
    fun freshDatabaseSearchesCatalogAndPersistsMealFoodsAndPortions() = runBlocking {
        open().use { database ->
            val repository = RoomMealEntryRepository(database.mealEntryDao())
            val allFoods = repository.searchFoods("")
            assertTrue(allFoods.any { it.category == FoodCategory.VEGETABLE })
            assertTrue(allFoods.any { it.category == FoodCategory.FRUIT })
            assertTrue(allFoods.any { it.category == FoodCategory.MEAT })
            assertTrue(allFoods.any { it.category == FoodCategory.LEGUME })
            assertEquals("Tomate", repository.searchFoods("tomate").single().germanName)

            repository.save(
                MealEntry(
                    id = "meal",
                    occurredAtEpochMillis = 123,
                    description = "Tomate, Oat milk",
                    note = "dinner",
                    portionSize = PortionSize.LARGE,
                    foods = listOf(
                        MealFood("tomato", "Tomate", "builtin-tomato"),
                        MealFood("oat-milk", "Oat milk", isCustom = true, portionOverride = PortionSize.SMALL),
                    ),
                ),
            )
            val saved = repository.observeAll().first().single()
            assertEquals(2, saved.foods.size)
            assertEquals(PortionSize.LARGE, saved.foods.first().effectivePortion(saved.portionSize))
            assertEquals(PortionSize.SMALL, saved.foods.last().effectivePortion(saved.portionSize))
            assertTrue(saved.foods.last().catalogFoodId != null)
            assertEquals("Oat milk", repository.searchFoods("OAT MILK").single().customName)

            repository.save(
                MealEntry(
                    id = "meal-2",
                    occurredAtEpochMillis = 124,
                    description = "oat MILK",
                    foods = listOf(MealFood("oat-milk-2", "oat MILK", isCustom = true)),
                ),
            )
            assertEquals("custom-oat milk", repository.observeAll().first().first().foods.single().catalogFoodId)
            assertEquals("Oat milk", repository.searchFoods("oat milk").single().customName)

            repository.delete("meal")
            assertEquals(1, repository.observeAll().first().size)
        }
    }

    @Test
    fun failedMealFoodInsertRollsBackMealAndCustomCatalogFood() = runBlocking {
        open().use { database ->
            RoomMealEntryRepository(database.mealEntryDao()).searchFoods("")
        }
        SQLiteDatabase.openDatabase(
            context.getDatabasePath(name).absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE,
        ).use { db ->
            db.execSQL("CREATE TRIGGER reject_meal_food BEFORE INSERT ON meal_foods BEGIN SELECT RAISE(ABORT, 'rejected'); END")
        }
        open().use { database ->
            val repository = RoomMealEntryRepository(database.mealEntryDao())
            var failed = false
            try {
                repository.save(
                    MealEntry(
                        "failed",
                        1,
                        "Unique custom food",
                        foods = listOf(MealFood("custom", "Unique custom food", isCustom = true)),
                    ),
                )
            } catch (expected: Exception) {
                failed = true
            }
            assertTrue(failed)
            assertTrue(repository.observeAll().first().isEmpty())
            assertTrue(repository.searchFoods("Unique custom food").isEmpty())
        }
    }

    @Test
    fun freshDatabaseStoresAllFlagCombinations() = runBlocking {
        open().use { database ->
            val repository = RoomStoolEntryRepository(database.stoolEntryDao())
            assertNull(repository.lastSavedBristolType())
            for (i in 0..3) {
                repository.save(StoolEntry("$i", i.toLong(), i + 1, null, i and 1 != 0, i and 2 != 0))
            }
        }
        open().use { database ->
            val entries = RoomStoolEntryRepository(database.stoolEntryDao()).observeAll().first()
            assertEquals(4, entries.size)
            for (i in 0..3) {
                assertEquals(StoolEntry("$i", i.toLong(), i + 1, null, i and 1 != 0, i and 2 != 0), entries.single { it.id == "$i" })
            }
        }
    }

    @Test
    fun failedDefaultUpdateRollsBackEntry() = runBlocking {
        open().use { it.stoolEntryDao().lastSavedBristolType() }
        SQLiteDatabase.openDatabase(context.getDatabasePath(name).absolutePath, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.execSQL("CREATE TRIGGER reject_default BEFORE INSERT ON stool_default BEGIN SELECT RAISE(ABORT, 'rejected'); END")
        }
        open().use { database ->
            val repository = RoomStoolEntryRepository(database.stoolEntryDao())
            var failed = false
            try {
                repository.save(StoolEntry("rollback", 0, 3))
            } catch (expected: Exception) {
                failed = true
            }
            assertTrue(failed)
            assertTrue(repository.observeAll().first().isEmpty())
            assertNull(repository.lastSavedBristolType())
        }
    }
}
