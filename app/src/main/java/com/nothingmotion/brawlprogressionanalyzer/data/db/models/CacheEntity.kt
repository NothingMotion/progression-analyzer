package com.nothingmotion.brawlprogressionanalyzer.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Cache entity to track when player data was last refreshed
 */
@Entity(
    tableName = "cache",
    indices = [Index(value = ["player_tag"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["player_tag"],
            childColumns = ["player_tag"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class CacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "player_tag")
    val playerTag: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "valid_for")
    val validFor: Date
)
