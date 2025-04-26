package com.nothingmotion.brawlprogressionanalyzer.data.remote.model
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Brawler
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Icon
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Player
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Progress
import java.util.Date

/**
 * Data model representing a player account in Brawl Stars
 */
data class APIHistory(
    val accountId: String,
    val name: String,
    val trophies: Int,
    val highestTrophies: Int,
    val level: Int,
    val icon: Icon? = null,
    val brawlers: List<Brawler> = emptyList(),
    val createdAt: Date = Date()
)
data class APIAccount(
    val account: Player,
//    val history: List<Player>? = null,
    val previousProgresses: List<Progress>? = null,
    val currentProgress: Progress,
    val futureProgresses: List<Progress>? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class APIAccountsResult(
    val accounts: List<APIAccount>
)

data class APIHistoryResult(

    val history: List<APIHistory>
)