package com.nothingmotion.brawlprogressionanalyzer.data.remote

import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIAccount
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.History
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ProgressionAnalyzerAPI {
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

    @GET("token/access")
    suspend fun getAccessToken(@Header("Authorization") authFrontEndHeader: String): String

    @GET("token/access/validate")
    suspend fun validateAccessToken(@Header("Authorization") authHeader: String) : Any
}