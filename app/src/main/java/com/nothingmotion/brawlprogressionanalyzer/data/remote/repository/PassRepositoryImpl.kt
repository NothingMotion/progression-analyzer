package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.util.DataErrorUtils
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PassRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.PassRepository
import javax.inject.Inject

class PassRepositoryImpl @Inject constructor(): PassRepository {
    @Inject lateinit var api: ProgressionAnalyzerAPI
    override suspend fun getPassFreeTable(token: String): Result<PassRewards, DataError.NetworkError> {
        try {
            return Result.Success(api.getPassFreeRewards("Bearer $token"))
        }catch(e :Exception){
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }

    override suspend fun getPassPremiumTable(token: String): Result<PassRewards, DataError.NetworkError> {
        try {
            return Result.Success(api.getPassPremiumRewards("Bearer $token"))
        }catch(e :Exception){
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }

    override suspend fun getPassPlusTable(token: String): Result<PassRewards, DataError.NetworkError> {
        try {
            return Result.Success(api.getPassPlusRewards("Bearer $token"))
        }catch(e :Exception){
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }
}