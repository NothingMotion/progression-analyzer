package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIToken
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.util.DataErrorUtils
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.TokenRepository
import javax.inject.Inject


class TokenRepositoryImpl @Inject constructor():  TokenRepository{
    @Inject lateinit var api: ProgressionAnalyzerAPI
    override suspend fun getAccessToken(frontEndToken: String): Result<APIToken, DataError.NetworkError> {
        try{
            return Result.Success(api.getAccessToken("Bearer $frontEndToken"))
        }
        catch(e: Exception){
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }

    override suspend fun validateAccessToken(accessToken: String): Result<String, DataError.NetworkError> {
        try{
            return Result.Success(api.validateAccessToken("Bearer $accessToken").toString())
        }catch(e: Exception){
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }
}
