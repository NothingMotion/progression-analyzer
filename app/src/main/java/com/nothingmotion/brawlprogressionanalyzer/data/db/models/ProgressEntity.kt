package com.nothingmotion.brawlprogressionanalyzer.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Progress
import java.util.Date

/**
 * Database entity for Progress in Brawl Stars
 */
@Entity(
    tableName = "progress",
    indices = [Index(value = ["account_id", "id"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class ProgressEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    
    @ColumnInfo(name = "account_id")
    val accountId: Int,
    
    @ColumnInfo(name = "player_tag")
    val playerTag: String,
    
    val coins: Int,
    val powerPoints: Int,
    val credits: Int,
    val gadgets: Int,
    val starPowers: Int,
    val gears: Int,
    val brawlers: Int,
    val averageBrawlerPower: Double,
    val averageBrawlerTrophies: Int,
    val isBoughtPass: Boolean,
    val isBoughtPassPlus: Boolean,
    val isBoughtRankedPass: Boolean,
    var type: ProgressType = ProgressType.PREVIOUS,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date
) {
    fun toDomain(): Progress {
        return Progress(
            coins = coins,
            powerPoints = powerPoints,
            credits = credits,
            gadgets = gadgets,
            starPowers = starPowers,
            gears = gears,
            brawlers = brawlers,
            averageBrawlerPower = averageBrawlerPower.toInt(),
            averageBrawlerTrophies = averageBrawlerTrophies,
            isBoughtPass = isBoughtPass,
            isBoughtPassPlus = isBoughtPassPlus,
            isBoughtRankedPass = isBoughtRankedPass,
            duration = createdAt
        )
    }

    companion object {
        fun fromDomain(progress: Progress, accountId: Int, playerTag: String): ProgressEntity {
            return ProgressEntity(
                id = 0,
                accountId = accountId,
                playerTag = playerTag,
                coins = progress.coins,
                powerPoints = progress.powerPoints,
                credits = progress.credits,
                gadgets = progress.gadgets,
                starPowers = progress.starPowers,
                gears = progress.gears,
                brawlers = progress.brawlers,
                averageBrawlerPower = progress.averageBrawlerPower.toDouble(),
                averageBrawlerTrophies = progress.averageBrawlerTrophies,
                isBoughtPass = progress.isBoughtPass,
                isBoughtPassPlus = progress.isBoughtPassPlus,
                isBoughtRankedPass = progress.isBoughtRankedPass,
                createdAt = progress.duration
            )
        }
    }
}




