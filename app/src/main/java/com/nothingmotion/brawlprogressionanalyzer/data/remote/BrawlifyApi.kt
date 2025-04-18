package com.nothingmotion.brawlprogressionanalyzer.data.remote

import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData
import retrofit2.http.GET
import retrofit2.http.Path

interface BrawlifyApi {
    @GET("brawlers")
    suspend fun getBrawlers() : List<BrawlerData>

    @GET("brawlers/{id}")
    suspend fun getBrawler(@Path("id") id: Long) : BrawlerData
}