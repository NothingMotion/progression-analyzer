package com.nothingmotion.brawlprogressionanalyzer.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cache", indices = [androidx.room.Index(value = ["id"], unique = true),
    androidx.room.Index(value = ["player_tag"], unique = true)])
data class CacheEntity(
    @PrimaryKey(autoGenerate =true) val id: Int,
    @ColumnInfo(name = "player_tag") val playerTag: String,
    val createdAt: Date,
    val validFor: Date
)
