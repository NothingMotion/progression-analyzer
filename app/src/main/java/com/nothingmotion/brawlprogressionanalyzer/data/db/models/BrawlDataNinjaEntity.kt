package com.nothingmotion.brawlprogressionanalyzer.data.db.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.GadgetDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarPowerDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Status
import java.util.Date

@Entity(tableName = "brawl_data_ninja", indices = [Index(value = ["id"], unique = true), Index(value = ["name"], unique = true)])
data class BrawlDataNinjaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val description: String,
    val rarity: String,
    val gadgets: List<GadgetDataNinja>,
    val starpowers: List<StarPowerDataNinja>,

    val createdAt: Date
)


fun BrawlDataNinjaEntity.toDomain(): com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerDataNinja {
    return com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerDataNinja(
        id = id.toString(),
        name = name,
        description = description,
        status = Status(rarity),
        gadgets = gadgets,
        starpowers = starpowers,
    )
}


fun BrawlerDataNinja.toDatabase(): BrawlDataNinjaEntity {
    return BrawlDataNinjaEntity(
        id = 0,
        name = name,
        description = description,
        rarity = "",
        gadgets = gadgets,
        starpowers = starpowers,
        createdAt= Date()
    )
}