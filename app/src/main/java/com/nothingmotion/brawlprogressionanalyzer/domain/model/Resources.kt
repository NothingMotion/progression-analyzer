package com.nothingmotion.brawlprogressionanalyzer.domain.model


open class Resource(
   @Transient open val name: String,
   @Transient open val amount: Int,
)

data class Coin(
    override val name: String = "Coin",
    override val amount: Int
): Resource(name,amount)
data class PowerPoint(
    override val name: String = "PowerPoint",
    override val amount: Int
) : Resource(name,amount)
data class Credit(
    override val name: String = "Credit",
    override val amount: Int
) : Resource(name,amount)
data class Gem(
    override val name: String = "Gem",
    override val amount: Int
): Resource(name,amount)

sealed class StarrDrop(
    override val name: String = "StarrDrop",
    override val amount: Int,
    open val rarity: RarityData
): Resource(name,amount)
data class RankedDrop(
    override val name: String = "RankedDrop",
    override val amount: Int,
    override val rarity: RarityData
): StarrDrop(name,amount,rarity)
data class Bling(

    override val name: String = "Bling",
    override val amount: Int
) : Resource(name,amount)
data class XPDoubler(
    override val name: String= "XP Doubler",
    override val amount: Int
): Resource(name,amount)
open class Skin(
    override val name: String = "Skin",
    override val amount: Int,
    open val rarity: RarityData
): Resource(name,amount)
data class ProSkin(
    override val name: String="Pro Skin",
    override val amount: Int,
    override val rarity: RarityData
): Skin(name,amount,rarity)


data class ProfileIcon(
    override val name: String = "ProfileIcon",
    override val amount: Int
): Resource(name,amount)
data class Pin(

    override val name: String  = "Pin",
    override val amount: Int
): Resource(name,amount)
data class Spray(
    override val name: String = "Spray",
    override val amount: Int
): Resource(name,amount)

data class BrawlerResource(
    override val name: String = "Brawler",
    override val amount: Int,

    val rarity: RarityData
) : Resource(name,amount)
open class AbilityResource(
    override val name: String,
    override val amount: Int
) : Resource(name,amount)
data class GadgetResource (
    override val name: String = "Gadget",
    override val amount: Int
) : AbilityResource(name,amount)

data class StarPowerResource(
    override val name: String = "StarPower",
    override val amount: Int
) : AbilityResource(name,amount)
data class GearResource (
    override val name: String = "Gear",
    override val amount: Int
) : AbilityResource(name,amount)
data class HyperCharge(
    override val name: String = "HyperCharge",
    override val amount: Int
): AbilityResource(name,amount)


