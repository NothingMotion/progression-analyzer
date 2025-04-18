package com.nothingmotion.brawlprogressionanalyzer.data.remote

import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIAccount
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIToken
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIUpdate
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.History
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PassRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Track
import com.nothingmotion.brawlprogressionanalyzer.domain.model.UpgradeTable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProgressionAnalyzerAPI {
    /////////////////////////////////
    @GET("accounts")
    suspend fun getAccounts(@Header("Authorization") authHeader: String): List<APIAccount>

    @GET("accounts/{tag}")
    suspend fun getAccount(
        @Path("tag") tag: String,
        @Header("Authorization") authHeader: String
    ): APIAccount

    @GET("accounts/{tag}/history")
    suspend fun getAccountHistory(
        @Path("tag") tag: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Header("Authorization") authHeader: String
    ): List<History>

    @GET("accounts/{tag}/refresh")
    suspend fun refreshAccount(
        @Path("tag") tag: String,
        @Header("Authorization") authHeader: String
    ): APIAccount

    @GET("accounts/refresh")
    suspend fun refreshAccounts(@Header("Authorization") authHeader: String): Any
    /////////////////////

    @GET("token/access")
    suspend fun getAccessToken(@Header("Authorization") authFrontEndHeader: String): APIToken

    @GET("token/access/validate")
    suspend fun validateAccessToken(@Header("Authorization") authHeader: String) : Any
    ///////////////////////
    @GET("table/brawler/rarity")
    suspend fun getBrawlerTable(@Header("Authorization") authHeader: String): List<BrawlerTable>

    @GET("table/brawler/upgrade")
    suspend fun getUpgradeTable(@Header("Authorization") authHeader: String) : UpgradeTable
    ////////////////////

    @GET("rewards/pass/free")
    suspend fun getPassFreeRewards(@Header("Authorization") authHeader: String) : PassRewards

    @GET("rewards/pass/premium")
    suspend fun getPassPremiumRewards(@Header("Authorization") authHeader: String) : PassRewards

    @GET("rewards/pass/plus")
    suspend fun getPassPlusRewards(@Header("Authorization") authHeader: String) : PassRewards

    @GET("rewards/starrdrop")
    suspend fun getStarrDropRewards(@Header("Authorization") authHeader: String) : List<StarrDropRewards>

    @GET("notmot/latest")
    suspend fun getLatestUpdate(@Header("Authorization") authHeader: String) : APIUpdate

    @POST("notmot/track")
    suspend fun newTrack(@Header("Authorization") authHeader: String, @Body body : Track) : Track


}