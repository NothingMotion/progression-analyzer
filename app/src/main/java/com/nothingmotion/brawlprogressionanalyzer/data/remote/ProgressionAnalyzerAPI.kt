package com.nothingmotion.brawlprogressionanalyzer.data.remote

import com.nothingmotion.brawlprogressionanalyzer.crashlytics.common.CrashLytics
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIAccount
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIAccountsResult
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIBrawlerTableResponse
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIHistoryResult
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIToken
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIUpdate
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIUpgradeTable
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIPassRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PassRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Track
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProgressionAnalyzerAPI {
    /////////////////////////////////
    @GET("accounts")
    suspend fun getAccounts(@Header("Authorization") authHeader: String, @Query("limit") limit: Int?=0, @Query("offset") offset: Int?=0): APIAccountsResult

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
    ): APIHistoryResult

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
    suspend fun getBrawlerTable(@Header("Authorization") authHeader: String): APIBrawlerTableResponse

    @GET("table/brawler/upgrade")
    suspend fun getUpgradeTable(@Header("Authorization") authHeader: String) : APIUpgradeTable
    ////////////////////

    @GET("rewards/pass/free")
    suspend fun getPassFreeRewards(@Header("Authorization") authHeader: String): APIPassRewards

    @GET("rewards/pass/premium")
    suspend fun getPassPremiumRewards(@Header("Authorization") authHeader: String): APIPassRewards

    @GET("rewards/pass/plus")
    suspend fun getPassPlusRewards(@Header("Authorization") authHeader: String): APIPassRewards

    @GET("rewards/starrdrop")
    suspend fun getStarrDropRewards(@Header("Authorization") authHeader: String) : List<StarrDropRewards>

    @GET("notmot/latest")
    suspend fun getLatestUpdate(@Header("Authorization") authHeader: String) : APIUpdate

    @POST("notmot/track")
    suspend fun newTrack(@Header("Authorization") authHeader: String, @Body body : Track) : Track


    @POST("notmot/crashlytics")
    suspend fun newCrash(@Header("Authorization") authHeader: String, @Body body: CrashLytics.CrashReport): CrashLytics.CrashReport

}
