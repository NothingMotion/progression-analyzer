package com.nothingmotion.brawlprogressionanalyzer.data.remote.model

import com.google.gson.annotations.SerializedName

data class APIPassRewards(
    val id: Int,
    val name: String,
    val resources: List<APIResource>
)

data class APIResource(
    val name: String,
    val amount: Int,
    val rarity: String? = null
) 