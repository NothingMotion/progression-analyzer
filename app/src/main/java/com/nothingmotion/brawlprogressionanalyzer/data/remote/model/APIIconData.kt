package com.nothingmotion.brawlprogressionanalyzer.data.remote.model

import com.google.gson.annotations.SerializedName


data class APIIcons(
    @SerializedName("player")
    val player: Map<String, APIPlayerIcon>
)

data class APIPlayerIcon(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("name2")
    val name2: String,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("imageUrl2")
    val imageUrl2: String,

    @SerializedName("brawler")
    val brawler: String? = null, // Assuming brawler could be a string or null

    @SerializedName("requiredTotalTrophies")
    val requiredTotalTrophies: Int,

    @SerializedName("sortOrder")
    val sortOrder: Int,

    @SerializedName("isReward")
    val isReward: Boolean,

    @SerializedName("isAvailableForOffers")
    val isAvailableForOffers: Boolean
)