package com.nothingmotion.brawlprogressionanalyzer.data.remote.model


data class APIUpdate(
    val name: String,
    val version: Long,
    val versionString: String,
    val forceUpdate: Boolean,
    val updateDescription: String,
    val url: String,
)
data class APIToken(
    val message: String?,
    val token: String,
)