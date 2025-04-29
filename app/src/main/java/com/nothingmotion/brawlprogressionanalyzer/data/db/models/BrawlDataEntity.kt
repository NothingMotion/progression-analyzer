package com.nothingmotion.brawlprogressionanalyzer.data.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Class
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Rarity

@Entity(tableName = "brawl_data")
data class BrawlDataEntity(
    @PrimaryKey val id: Int,
    val avatarId: Int,
    val className: String,
    val description: String,
    val name: String,
    val rarity: String,
    val released: Boolean,
    val imageUrl: String,
)


// TODO: Implement all members of BrawlerData





fun BrawlDataEntity.toDomain() : BrawlerData {
    return BrawlerData(
        id = id,
        avatarId = avatarId,
        `class` = com.nothingmotion.brawlprogressionanalyzer.domain.model.Class(
            id = avatarId,
            name = className,
        ),
        description = description,
        name = name,
        rarity = Rarity(
            id = avatarId,
            name = rarity,
            color = ""
        ),
        released = released,
        imageUrl = imageUrl,
        imageUrl2 = "",
        imageUrl3 = "",
        gadgets = emptyList(),
        starPowers = emptyList(),
        version = 0,
        descriptionHtml = "",
        fankit = "",
        hash = "",
        link = "",
        path = "",
        unlock = "",
        videos = emptyList()
    )
}


fun BrawlerData.toEntity() : BrawlDataEntity {
    return BrawlDataEntity(
        id = id,
        avatarId = avatarId,
        className = `class`.name,
        description = description,
        name = name,
        rarity = rarity.name,
        released = released,
        imageUrl = imageUrl
    )
}