package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.UpgradeTable
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.UpgradeTableRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UpgradeTableRepositoryImpl : UpgradeTableRepository {
    @Inject lateinit var api: ProgressionAnalyzerAPI
    override suspend fun getUpgradeTable(token: String): Result<UpgradeTable, DataError.NetworkError> {
        try {
            return Result.Success(api.getUpgradeTable(token))
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
        catch (e: Exception) {
            return Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }
}