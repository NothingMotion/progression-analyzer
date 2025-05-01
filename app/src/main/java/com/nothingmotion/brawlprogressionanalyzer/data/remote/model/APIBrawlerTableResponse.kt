package com.nothingmotion.brawlprogressionanalyzer.data.remote.model

data class APIBrawlerTableResponse(
    val table: List<APIBrawlerTable>
)

data class APIBrawlerTable(
    val name: String,
    val value: Int
)