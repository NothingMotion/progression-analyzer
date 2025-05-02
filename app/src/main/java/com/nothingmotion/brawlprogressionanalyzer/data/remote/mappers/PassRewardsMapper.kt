package com.nothingmotion.brawlprogressionanalyzer.data.remote.mappers

import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIPassRewards
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIResource
import com.nothingmotion.brawlprogressionanalyzer.domain.model.*

fun APIPassRewards.toDomain(): PassRewards {
    val domainResources = resources.map { it.toDomainResource() }
    
    return when (name) {
        "BrawlPassPlus" -> BrawlPassPlusRewards(id, name, domainResources)
        else -> BrawlPassRewards(id, name, domainResources)
    }
}

fun APIResource.toDomainResource(): Resource {
    return when (name) {
        "Coins", "Coin" -> Coin(amount = amount)
        "PowerPoints", "PowerPoint" -> PowerPoint(amount = amount)
        "Credit" -> Credit(amount = amount)
        "Gem" -> Gem(amount = amount)
        "StarrDrop" -> {
            val rarityData = when (rarity) {
                "Common" -> RarityData.COMMON
                "Rare" -> RarityData.RARE
                "Super Rare" -> RarityData.SUPER_RARE
                "Epic" -> RarityData.EPIC
                "Mythic" -> RarityData.MYTHIC
                "Legendary" -> RarityData.LEGENDARY
                else -> RarityData.COMMON
            }
            RankedDrop(name = name, amount = amount, rarity = rarityData)
        }
        "Bling" -> Bling(amount = amount)
        "XP Doubler" -> XPDoubler(amount = amount)
        "ProfileIcon" -> ProfileIcon(amount = amount)
        "Pin" -> Pin(amount = amount)
        "Spray" -> Spray(amount = amount)
        "Brawler" -> {
            val rarityData = when (rarity) {
                "Common" -> RarityData.COMMON
                "Rare" -> RarityData.RARE
                "Super Rare" -> RarityData.SUPER_RARE
                "Epic" -> RarityData.EPIC
                "Mythic" -> RarityData.MYTHIC
                "Legendary" -> RarityData.LEGENDARY
                else -> RarityData.COMMON
            }
            BrawlerResource(amount = amount, rarity = rarityData)
        }
        "Gadget" -> GadgetResource(amount = amount)
        "StarPower" -> StarPowerResource(amount = amount)
        "Gear" -> GearResource(amount = amount)
        "HyperCharge" -> HyperCharge(amount = amount)
        else -> {
            // Default to basic resource if unknown
            object : Resource(name, amount) {}
        }
    }
} 