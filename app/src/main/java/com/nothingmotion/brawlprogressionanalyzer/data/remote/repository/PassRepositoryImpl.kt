package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.data.remote.mappers.toDomain
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PassRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.PassRepository
import com.nothingmotion.brawlprogressionanalyzer.util.DataErrorUtils
import timber.log.Timber
import javax.inject.Inject

class PassRepositoryImpl @Inject constructor(): PassRepository {
    @Inject lateinit var api: ProgressionAnalyzerAPI
    
    override suspend fun getPassFreeTable(token: String): Result<PassRewards, DataError.NetworkError> {
        try {
            val apiResponse = api.getPassFreeRewards("Bearer $token")
            return Result.Success(apiResponse.toDomain())
        } catch(e: Exception) {
            Timber.tag("PassRepositoryImpl").e("error while getting pass free: $e")
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }

    override suspend fun getPassPremiumTable(token: String): Result<PassRewards, DataError.NetworkError> {
        try {
            val apiResponse = api.getPassPremiumRewards("Bearer $token") 
            return Result.Success(apiResponse.toDomain())
        } catch(e: Exception) {
            Timber.tag("PassRepositoryImpl").e("error while getting pass premium: $e")
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }

    override suspend fun getPassPlusTable(token: String): Result<PassRewards, DataError.NetworkError> {
        try {
            val apiResponse = api.getPassPlusRewards("Bearer $token")
            return Result.Success(apiResponse.toDomain())
        } catch(e: Exception) {
            Timber.tag("PassRepositoryImpl").e("error while getting pass plus: $e")
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }
}