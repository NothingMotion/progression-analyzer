package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerTableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class BrawlerTableRepositoryImpl @Inject constructor(): BrawlerTableRepository {
    @Inject lateinit var api: ProgressionAnalyzerAPI
    override suspend fun getBrawlerTable(token: String): Flow<Result<List<BrawlerTable>,DataError.NetworkError>> {
        return flow {
            try {
                emit(Result.Success(api.getBrawlerTable("Bearer $token")))
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
            catch (e: Exception) {
                emit(Result.Error(DataError.NetworkError.UNKNOWN))
            }
        }

    }
}