package com.nothingmotion.brawlprogressionanalyzer.data.remote.mappers

import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIBrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.RarityData


fun APIBrawlerTable.toBrawlerTable() : BrawlerTable {

    val rarity = when(this.name){
        "Common" -> RarityData.COMMON
        "Rare" -> RarityData.RARE
        "Super Rare" -> RarityData.SUPER_RARE
        "Epic" -> RarityData.EPIC
        "Mythic" -> RarityData.MYTHIC
        "Legendary" -> RarityData.LEGENDARY
        else -> RarityData.COMMON
    }
    return BrawlerTable(
        rarity = rarity,
        value = this.value
    )
}