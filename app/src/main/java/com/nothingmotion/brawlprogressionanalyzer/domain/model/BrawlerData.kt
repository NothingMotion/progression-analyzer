package com.nothingmotion.brawlprogressionanalyzer.domain.model

data class Class(
    val id: Int,
    val name: String
)
open class AbilityData(
    open val description: String,
    open val descriptionHtml: String,
    open val id: Long,
    open val imageUrl: String,
    open val name: String,
    open val path: String,
    open val released: Boolean,
    open val version: Int
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