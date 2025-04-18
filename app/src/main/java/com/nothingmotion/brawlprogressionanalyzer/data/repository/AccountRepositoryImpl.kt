package com.nothingmotion.brawlprogressionanalyzer.data.repository

import com.nothingmotion.brawlprogressionanalyzer.data.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.data.mappers.toAccount
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import com.nothingmotion.brawlprogressionanalyzer.util.TokenManager
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AccountRepositoryImpl constructor(@Inject val api: ProgressionAnalyzerAPI,@Inject val tokenManager: TokenManager): AccountRepository {
    override suspend fun getAccount(tag: String):Result<Account, DataError.NetworkError> {
        try {
            val token = tokenManager.getAccessToken("")
            return Result.Success(api.getAccount(tag,token).toAccount())
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
        catch(e : Exception){
            return Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }

    override suspend fun getAllAccounts(): List<Account> {
        TODO("Not yet implemented")
    }
}