package com.nothingmotion.brawlprogressionanalyzer.data.remote.model

import java.util.Date


data class APIUpdate(
    val name: String,
    val version: Long,
    val versionString: String,
    val forceUpdate: Boolean,
    val updateDescription: String,
    val url: String,
)
data class APITokenResponse (
    val token : String,
    val expiresAt:Date,
)
data class APIToken(
    val message: String?,
    val response:APITokenResponse
)