package com.nothingmotion.brawlprogressionanalyzer.data.remote

import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerDataNinja
import retrofit2.http.GET
import retrofit2.http.Path

interface BrawlNinjaApi {
    @GET("brawlers/{name}/data.json")
    suspend fun getBrawlerData(@Path("name") name: String) : BrawlerDataNinja

}
