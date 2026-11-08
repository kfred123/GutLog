package de.gutlog.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stool_default")
data class StoolDefaultEntity(
    @PrimaryKey val id: Int = 1,
    val bristolType: Int,
)
