package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import android.util.Log
import com.nothingmotion.brawlprogressionanalyzer.crashlytics.common.CrashLytics
import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIUpdate
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.util.DataErrorUtils
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Track
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.NotMotRepository
import javax.inject.Inject

class NotMotRepositoryImpl @Inject constructor() : NotMotRepository {

    @Inject lateinit var api: ProgressionAnalyzerAPI
    override suspend fun getLatestUpdate(token: String): Result<APIUpdate, DataError.NetworkError> {
        try {
            return Result.Success(api.getLatestUpdate("Bearer $token"))
        } catch(e :Exception){
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }

    override suspend fun trackUser(
        token: String,
        data: Track
    ): Result<Track, DataError.NetworkError> {
        try {
            return Result.Success(api.newTrack("Bearer $token",data))
        } catch(e: Exception){
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }

    override suspend fun reportCrash(
        token: String,
        data: CrashLytics.CrashReport
    ): Result<CrashLytics.CrashReport, DataError.NetworkError> {
        return try{
            Result.Success(api.newCrash("Bearer $token",data))
        }catch (e:Exception){

            Log.e("NotMotRepositoryImpl",e.stackTraceToString())
            Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }
}