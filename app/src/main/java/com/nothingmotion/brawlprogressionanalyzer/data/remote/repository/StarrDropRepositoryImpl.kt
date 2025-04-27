package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.util.DataErrorUtils
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.StarrDropRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StarrDropRepositoryImpl @Inject constructor(): StarrDropRepository {



    @Inject lateinit var api: ProgressionAnalyzerAPI
    override suspend fun getStarrDropRewards(token: String): Flow<Result<List<StarrDropRewards>, DataError.NetworkError>> {
        return flow {
            try {
                emit(Result.Success(api.getStarrDropRewards("Bearer $token")))
            }catch(e: Exception){
                emit(Result.Error(DataErrorUtils.handleHttpException(e)))
            }
        }
    }
}