package com.nothingmotion.brawlprogressionanalyzer.domain.model

import com.google.gson.annotations.SerializedName
import java.util.Date
import java.util.UUID

/**
 * Data model representing a player account in Brawl Stars
 */
sealed class Ability(
    @Transient open val id: Long,
    @Transient open val name: String
)

data class Gear(
    @SerializedName("id") override val id: Long,
    override val name: String
) : Ability(id, name)

data class StarPower(
    @SerializedName("id") override val id: Long,
    override val name: String
) : Ability(id, name)

data class Gadget(
    @SerializedName("id") override val id: Long,
    override val name: String
) : Ability(id, name)

data class Brawler(
    val id: Long,
    val name: String,
    val trophies: Int,
    val highestTrophies: Int,
    val rank: Int,
    val power: Int,
    val gears: List<Gear>? = null,
    val starPowers: List<StarPower>? = null,
    val gadgets: List<Gadget>? = null
)

data class Icon(
    val id: Long,
    val url: String
)

data class Player(
//    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val tag: String,
    val trophies: Int,
    val highestTrophies: Int,
    val level: Int,
    val icon: Icon? = null,
    val brawlers: List<Brawler> = emptyList(),
    val createdAt: Date = Date()
)
data class Account(
    val account: Player,
    val history: List<Player>? = null,
    val previousProgresses: List<Progress>? = null,
    val currentProgress: Progress,
    val futureProgresses: List<Progress>? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)