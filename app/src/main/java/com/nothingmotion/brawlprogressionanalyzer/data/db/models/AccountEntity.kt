package com.nothingmotion.brawlprogressionanalyzer.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Embedded
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Brawler
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Icon
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Player
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Progress
import java.util.Date

/**
 * Database entity for Account in Brawl Stars
 * This is the main entity that owns all relationships
 */
@Entity(
    tableName = "account",
    indices = [
        Index(value = ["player_tag"], unique = true)
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "player_tag")
    val playerTag: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date
)

/**
 * Database entity for history player entities for an account
 */
@Entity(
    tableName = "player_history",
    primaryKeys = ["account_id", "player_tag"],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["tag"],
            childColumns = ["player_tag"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["account_id"]),
        Index(value = ["player_tag"])
    ]
)
data class PlayerHistoryEntity(
    @ColumnInfo(name = "account_id")
    val accountId: Int,
    
    @ColumnInfo(name = "player_tag")
    val playerTag: String
)

enum class ProgressType {
    PREVIOUS,
    FUTURE,
    CURRENT
}

/**
 * Data class containing all the Account data with relations
 */
data class AccountWithRelations(
    @Embedded val account: AccountEntity,

    @Relation(
        parentColumn = "player_tag",
        entityColumn = "tag"
    )
    val player: PlayerEntity,

    @Relation(
        entity = PlayerEntity::class,
        parentColumn = "id",
        entityColumn = "tag",
        associateBy = Junction(
            value = PlayerHistoryEntity::class,
            parentColumn = "account_id",
            entityColumn = "player_tag"
        )
    )
    val historyPlayers: List<PlayerEntity>,
    
    @Relation(
        entity = ProgressEntity::class,
        parentColumn = "id",
        entityColumn = "account_id"
    )
    val progressHistories: List<ProgressEntity>
) {
    fun toDomain(): Account {
        val previousProgresses = progressHistories
            .filter { it.type == ProgressType.PREVIOUS }
            .map { it.toDomain() }

        val futureProgresses = progressHistories
            .filter { it.type == ProgressType.FUTURE }
            .map { it.toDomain() }
        
        val currentProgress = progressHistories
            .firstOrNull { it.type == ProgressType.CURRENT }
            ?.toDomain() ?: previousProgresses.firstOrNull() ?: Progress(
                coins = 0,
                powerPoints = 0,
                credits = 0,
                gadgets = 0,
                starPowers = 0,
                gears = 0,
                brawlers = 0,
                averageBrawlerPower = 0,
                averageBrawlerTrophies = 0,
                isBoughtPass = false,
                isBoughtPassPlus = false,
                isBoughtRankedPass = false,
                duration = Date()
            )

        return Account(
            account = player.toDomain(),
            history = historyPlayers.map { it.toDomain() },
            previousProgresses = previousProgresses,
            currentProgress = currentProgress,
            futureProgresses = futureProgresses,
            createdAt = account.createdAt,
            updatedAt = account.updatedAt
        )
    }
}