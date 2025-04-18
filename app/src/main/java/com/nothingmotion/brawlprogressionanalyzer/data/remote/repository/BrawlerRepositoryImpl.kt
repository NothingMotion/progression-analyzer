package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.BrawlifyApi
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class BrawlerRepositoryImpl @Inject constructor() : BrawlerRepository {
    @Inject lateinit var api : BrawlifyApi
    override suspend fun getBrawler(id: Long): Result<BrawlerData,DataError.NetworkError> {
        try {
            return Result.Success(api.getBrawler(id))
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
        catch(e: Exception){
            return Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }

    override suspend fun getBrawlers(): Flow<Result<List<BrawlerData>,DataError.NetworkError>> {
        return flow {
            try {
                emit(Result.Success(api.getBrawlers()))
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
            catch(e:Exception){
                emit(Result.Error(DataError.NetworkError.UNKNOWN))
            }
        }
    }
}