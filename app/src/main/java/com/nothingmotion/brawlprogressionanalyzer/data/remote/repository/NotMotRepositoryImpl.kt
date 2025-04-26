package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import android.util.Log
import com.nothingmotion.brawlprogressionanalyzer.crashlytics.common.CrashLytics
import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIUpdate
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Track
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.NotMotRepository
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class NotMotRepositoryImpl @Inject constructor() : NotMotRepository {

    @Inject lateinit var api: ProgressionAnalyzerAPI
    override suspend fun getLatestUpdate(token: String): Result<APIUpdate, DataError.NetworkError> {
        try {
            return Result.Success(api.getLatestUpdate("Bearer $token"))
        }
        catch(e: IOException){
            return Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION)
        }
        catch(e: HttpException){
            return when (e.code()){
                400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                else -> Result.Error(DataError.NetworkError.UNKNOWN)
            }
        }
        catch(e :Exception){
            return Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }

    override suspend fun trackUser(
        token: String,
        data: Track
    ): Result<Track, DataError.NetworkError> {
        try {
            return Result.Success(api.newTrack("Bearer $token",data))
        }
        catch(e: IOException){
            return Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION)
        }
        catch(e: HttpException){
            return when (e.code()){
                400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                else -> Result.Error(DataError.NetworkError.UNKNOWN)
            }
        }
        catch(e: Exception){
            return Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }

    override suspend fun reportCrash(
        token: String,
        data: CrashLytics.CrashReport
    ): Result<CrashLytics.CrashReport, DataError.NetworkError> {
        return try{
            Result.Success(api.newCrash("Bearer $token",data))
        }
        catch(e: IOException){
            return Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION)
        }
        catch(e: HttpException){
            return when (e.code()){
                400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                else -> {
                    Timber.tag("NotMotRepositoryImpl").e(e);
                    Result.Error(DataError.NetworkError.UNKNOWN)
                }
            }
        }
        catch (e:Exception){

            Log.e("NotMotRepositoryImpl",e.stackTraceToString())
            Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }
}