package com.nothingmotion.brawlprogressionanalyzer.data.db.models

import androidx.room.Entity
import com.nothingmotion.brawlprogressionanalyzer.domain.model.GadgetDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarPowerDataNinja

@Entity(tableName = "brawl_data_ninja")
data class BrawlDataNinjaEntity(
    val id: String,
    val name: String,
    val description: String,
    val rarity: String,
    val gadgets: List<GadgetDataNinja>,
    val starpowers: List<StarPowerDataNinja>
)
