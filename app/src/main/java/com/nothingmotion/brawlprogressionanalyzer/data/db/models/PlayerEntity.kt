package com.nothingmotion.brawlprogressionanalyzer.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Brawler
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Icon
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Player
import java.util.Date

/**
 * Database entity for a Player in Brawl Stars
 * This entity is independent and has no foreign keys to other entities
 */
@Entity(
    tableName = "player",
    indices = [Index(value = ["tag"], unique = true)]
)
data class PlayerEntity(
    @PrimaryKey
    var tag: String,
    
    val name: String,
    val trophies: Int,
    val highestTrophies: Int,
    val level: Int,
    
    @ColumnInfo(name = "icon_id")
    val iconId: Long?,
    
    val brawlers: List<Brawler>,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date
) {
    fun toDomain(): Player {
        val icon = if (iconId != null) {
            Icon(iconId, "")
        } else null

        return Player(
            name = name,
            tag = tag,
            trophies = trophies,
            highestTrophies = highestTrophies,
            level = level,
            icon = icon,
            brawlers = brawlers,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(player: Player): PlayerEntity {
            return PlayerEntity(
                tag = player.tag,
                name = player.name,
                trophies = player.trophies,
                highestTrophies = player.highestTrophies,
                level = player.level,
                iconId = player.icon?.id,
                brawlers = player.brawlers,
                createdAt = player.createdAt
            )
        }
    }
}