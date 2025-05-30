package com.nothingmotion.brawlprogressionanalyzer.domain.model

import java.util.Date

data class Progress(
    val coins: Int,
    val powerPoints: Int,
    val credits: Int,
    val gears: Int,

    val starPowers: Int,
    val gadgets: Int = 0,
    val brawlers: Int,
    val averageBrawlerPower: Int,
    val averageBrawlerTrophies: Int,
    val isBoughtPass: Boolean,
    val isBoughtPassPlus: Boolean,
    val isBoughtRankedPass: Boolean,
    val duration: Date
)
