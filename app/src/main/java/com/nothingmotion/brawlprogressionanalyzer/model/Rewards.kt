package com.nothingmotion.brawlprogressionanalyzer.model


sealed class Rewards (
    open val id : Int,
    open val name: String,
)

sealed class PassRewards (
    override val id: Int,
    override val name: String = "PassReward",
    open val resources: List<Resource>
) : Rewards(id,name)
data class BrawlPassRewards(
    override val id: Int,
    override val name: String,
    override val resources: List<Resource>
) : PassRewards(id,name,resources)
data class BrawlPassPlusRewards(
    override val id: Int,
    override val name: String,
    override val resources: List<Resource>
) : PassRewards(id,name,resources)
data class StarrDropReward(
    val resource: Resource,
    var chance: Float
)
data class StarrDropRewards(
    override val id: Int,
    override val name: String,
    val rarity: RarityData,
    var chanceToDrop: Float,
    val rewards: List<StarrDropReward>
) : Rewards(id,name)