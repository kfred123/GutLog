package de.gutlog.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val DIARY_MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE stool_entries ADD COLUMN hasBlood INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE stool_entries ADD COLUMN isUrgent INTEGER NOT NULL DEFAULT 0")
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS stool_default (id INTEGER NOT NULL, bristolType INTEGER NOT NULL, PRIMARY KEY(id))",
        )
        connection.execSQL(
            "INSERT INTO stool_default (id, bristolType) " +
                "SELECT 1, bristolType FROM stool_entries ORDER BY occurredAtEpochMillis DESC, id DESC LIMIT 1",
        )
    }
}
