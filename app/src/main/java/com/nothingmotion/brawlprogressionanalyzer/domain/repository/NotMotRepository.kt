package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.crashlytics.common.CrashLytics
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIUpdate
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Track


interface NotMotRepository {
    suspend fun getLatestUpdate(token: String) : Result<APIUpdate,DataError.NetworkError>

    suspend fun trackUser(token:String, data: Track): Result<Track,DataError.NetworkError>

    suspend fun reportCrash(token: String, data: CrashLytics.CrashReport): Result<CrashLytics.CrashReport,DataError.NetworkError>
}