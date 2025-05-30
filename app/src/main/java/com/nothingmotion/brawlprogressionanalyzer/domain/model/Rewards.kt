package com.nothingmotion.brawlprogressionanalyzer.domain.model


open class Rewards (
   @Transient open val id : Int,
   @Transient open val name: String,
)

open class PassRewards (
    override val id: Int,
    override val name: String = "PassReward",
    open val resources: List<Resource>
) : Rewards(id,name)
data class BrawlPassRewards(
    override val id: Int,
    override val name: String = "PassPremiumReward",
    override val resources: List<Resource>
) : PassRewards(id,name,resources)
data class BrawlPassPlusRewards(
    override val id: Int,
    override val name: String = "PassPlusReward",
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