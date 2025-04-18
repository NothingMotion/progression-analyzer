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
    val url: String
)

sealed class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val tag: String,
    val trophies: Int,
    val highestTrophies: Int,
    val level: Int,
    val icon: Icon? = null,
    val brawlers: List<Brawler> = emptyList(),
    val createdAt: Date = Date()
)
sealed class History(
    id: String = UUID.randomUUID().toString(),
    name: String,
    tag: String,
    trophies: Int,
    highestTrophies: Int,
    level: Int,
    icon: Icon? = null,
    brawlers: List<Brawler> = emptyList(),
    createdAt: Date = Date()
): Player(id, name, tag, trophies, highestTrophies, level, icon, brawlers, createdAt)
data class APIAccount(
    val account: Player,
//    val history: List<Player>? = null,
    val previousProgresses: List<Progress>? = null,
    val currentProgress: Progress,
    val futureProgresses: List<Progress>? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)