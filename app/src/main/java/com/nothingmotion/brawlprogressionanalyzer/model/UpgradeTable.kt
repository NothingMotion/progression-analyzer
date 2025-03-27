package com.nothingmotion.brawlprogressionanalyzer.model
data class UpgradeTableLevel(

    val level: Int,
    val coins: Int,
    val powerPoints: Int,
    val totalCoins: Int,
    val totalPowerPoints: Int
)
data class UpgradeTable(
    val levels: List<UpgradeTableLevel>
)
