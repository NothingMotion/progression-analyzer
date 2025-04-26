package com.nothingmotion.brawlprogressionanalyzer.data.remote

import android.graphics.Bitmap
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIIcons
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData
import retrofit2.http.GET
import retrofit2.http.Path

interface BrawlifyApi {
    @GET("brawlers")
    suspend fun getBrawlers() : List<BrawlerData>

    @GET("brawlers/{id}")
    suspend fun getBrawler(@Path("id") id: Long) : BrawlerData

    @GET("icons")
    suspend fun getIcons(): APIIcons

    @GET("gadgets/borderless/{id}")
    suspend fun getGadget(@Path("id") id: String) : ByteArray

    @GET("star-powers/borderless/{id}")
    suspend fun getStarPower(@Path("id") id: String) : ByteArray

    @GET("gears/borderless/{id}")
    suspend fun getGear(@Path("id") id: String) : ByteArray

    @GET("tiers/regular/{id}")
    suspend fun getTier(@Path("id") id: String) : ByteArray
}