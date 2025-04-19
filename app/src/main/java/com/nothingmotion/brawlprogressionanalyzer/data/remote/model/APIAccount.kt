package com.nothingmotion.brawlprogressionanalyzer.data.remote.model
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Brawler
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Progress
import java.util.Date
import java.util.UUID

/**
 * Data model representing a player account in Brawl Stars
 */

data class Icon(
    val id: Long,
//    val url: String
)

open class Player(
//    val id: String = UUID.randomUUID().toString(),
    open val name: String,
    open val tag: String,
    open val trophies: Int,
    open val highestTrophies: Int,
    open val level: Int,
    open val icon: Icon? = null,
    open val brawlers: List<Brawler> = emptyList(),
    open val createdAt: Date = Date()
)
data class History(
//    id: String = UUID.randomUUID().toString(),
    override val name: String,
    override val tag: String,
    override val trophies: Int,
    override val highestTrophies: Int,
    override val level: Int,
    override val icon: Icon? = null,
    override val brawlers: List<Brawler> = emptyList(),
    override val createdAt: Date = Date()
): Player(name, tag, trophies, highestTrophies, level, icon, brawlers, createdAt)
data class APIAccount(
    val account: Player,
//    val history: List<Player>? = null,
    val previousProgresses: List<Progress>? = null,
    val currentProgress: Progress,
    val futureProgresses: List<Progress>? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)