package com.nothingmotion.brawlprogressionanalyzer.domain.model

import com.nothingmotion.brawlprogressionanalyzer.domain.model.RarityData.LEGENDARY

data class Class(
    val id: Int,
    val name: String
)
open class AbilityData(
    @Transient open val description: String,
    @Transient open val descriptionHtml: String,
    @Transient open val id: Long,
    @Transient open val imageUrl: String,
    @Transient open val name: String,
    @Transient open val path: String,
    @Transient open val released: Boolean,
    @Transient open val version: Int
)
data class GadgetData(
    override val description: String,
    override val descriptionHtml: String,
    override val id: Long,
    override val imageUrl: String,
    override val name: String,
    override val path: String,
    override val released: Boolean,
    override val version: Int
) : AbilityData(
    description,
    descriptionHtml,
    id,
    imageUrl,
    name,
    path,
    released,
    version
)


data class StarPowerData(
    val description: String,
    val descriptionHtml: String,
    val id: Int,
    val imageUrl: String,
    val name: String,
    val path: String,
    val released: Boolean,
    val version: Int
)
data class Rarity(
    val color: String,
    val id: Int,
    val name: String
)

fun Rarity.toRarityData(): RarityData{
    return when(this.name){
        "Common" -> RarityData.COMMON
        "Rare" -> RarityData.RARE
        "Super Rare" -> RarityData.SUPER_RARE
        "Epic" -> RarityData.EPIC
        "Mythic" -> RarityData.MYTHIC
        "Legendary" -> LEGENDARY
        else -> RarityData.COMMON
    }
}
data class BrawlerData(
    val id: Int,
    val avatarId: Int,
    val `class`: Class,
    val description: String,
    val descriptionHtml: String,
    val fankit: String,
    val gadgets: List<GadgetData>,
    val hash: String,
    val imageUrl: String,
    val imageUrl2: String,
    val imageUrl3: String,
    val link: String,
    val name: String,
    val path: String,
    val rarity: Rarity,
    val released: Boolean,
    val starPowers: List<StarPowerData>,
    val unlock: Any,
    val version: Int,
    val videos: List<Any>
)







data class BrawlersDataResponse(
    val list: List<BrawlerData>
)