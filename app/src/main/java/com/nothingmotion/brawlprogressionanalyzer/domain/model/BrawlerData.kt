package com.nothingmotion.brawlprogressionanalyzer.domain.model

data class Class(
    val id: Int,
    val name: String
)
data class GadgetData(
    val description: String,
    val descriptionHtml: String,
    val id: Int,
    val imageUrl: String,
    val name: String,
    val path: String,
    val released: Boolean,
    val version: Int
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