package com.nothingmotion.brawlprogressionanalyzer.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Embedded
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Gadget
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Gear
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Icon
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Player
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarPower


@Entity(tableName="brawler", primaryKeys = ["id","player_tag"], indices = [
    androidx.room.Index(value = ["id","player_tag"], unique = true)
])
data class BrawlerEntity(
    val id: Long,
    @ColumnInfo(name="player_tag") val tag: String,
    val name: String,
    val trophies: Int,
    val highestTrophies: Int,
    val rank: Int,
    val power: Int,
    val gears: List<Gear>? = null,
    val starPowers: List<StarPower>? = null,
    val gadgets: List<Gadget>? = null
) {
}
@Entity(tableName = "player", primaryKeys = ["tag"], indices = [
    androidx.room.Index(value = ["tag"], unique = true)
])
data class PlayerEntity(
    val name: String,
    val tag: String,
    val trophies: Int,
    val highestTrophies: Int,
    val level: Int,
    val iconId: Long,
    val iconUrl: String
) {
    fun toDomain(): Player {
        return Player(
            name = name,
            tag = tag,
            trophies = trophies,
            highestTrophies = highestTrophies,
            level = level,
            icon = Icon(iconId, iconUrl)
        )
    }
}

data class PlayerWithBrawlers(
    @Embedded val player: PlayerEntity,
    @Relation(
        parentColumn = "tag",
        entityColumn = "player_tag",
        entity = BrawlerEntity::class
    )
    val brawlers: List<BrawlerEntity>?
)

@Entity(tableName = "account",

)
data class AccountEntity (
    @PrimaryKey(true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "account_tag") val accountTag: String
) {
}

data class AccountData(
    @Embedded val account: AccountEntity,
    @Relation(
        parentColumn = "account_tag",
        entityColumn = "tag",
        entity = PlayerEntity::class
    )
    val player: PlayerWithBrawlers?
){}