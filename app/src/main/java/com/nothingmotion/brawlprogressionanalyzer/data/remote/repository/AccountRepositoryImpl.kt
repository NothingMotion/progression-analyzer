    package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.data.remote.mappers.toAccount
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.History
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import com.nothingmotion.brawlprogressionanalyzer.util.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(

) : AccountRepository {

    @Inject
    lateinit var api: ProgressionAnalyzerAPI
    @Inject
    lateinit var tokenManager: TokenManager
    override suspend fun getAccount(tag: String): Result<Account, DataError.NetworkError> {
        try {
            val token = tokenManager.getAccessToken("")
            return Result.Success(api.getAccount(tag, token).toAccount())
        } catch (e: IOException) {
            return Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION)
        } catch (e: HttpException) {
            return when (e.code()) {
                400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                else -> Result.Error(DataError.NetworkError.UNKNOWN)
            }
        } catch (e: Exception) {
            return Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }

    override suspend fun getAccountHistory(
        tag: String,
        limit: Int?,
        offset: Int?
    ): Flow<Result<List<History>, DataError.NetworkError>> {
        return flow {
            try {
                val token = tokenManager.getAccessToken("")
                emit(Result.Success(api.getAccountHistory(tag, limit, offset, token)))
            } catch (e: IOException) {
                emit(Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION))
            } catch (e: HttpException) {
                emit(
                    when (e.code()) {
                        400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                        401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                        403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                        429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                        500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                        else -> Result.Error(DataError.NetworkError.UNKNOWN)
                    }
                )
            } catch (e: Exception) {
                emit(Result.Error(DataError.NetworkError.UNKNOWN))
            }
        }
    }

    override suspend fun getAllAccounts(): Flow<Result<List<Account>,DataError.NetworkError>> {
        return flow {
            try {
                emit(Result.Success(api.getAccounts().map { it.toAccount() }))
            }
            catch (e: IOException) {
                emit(Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION))
            } catch (e: HttpException) {
                emit(
                    when (e.code()) {
                        400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                        401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                        403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                        429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                        500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                        else -> Result.Error(DataError.NetworkError.UNKNOWN)
                    }
                )
            }
            catch(e: Exception){
                emit(Result.Error(DataError.NetworkError.UNKNOWN))
            }
        }
    }
}