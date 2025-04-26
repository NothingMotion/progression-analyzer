package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.BrawlNinjaApi
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlNinjaRepository
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class BrawlNinjaRepositoryImpl @Inject constructor() : BrawlNinjaRepository {
    @Inject
    lateinit var api: BrawlNinjaApi

    override suspend fun getBrawlerData(name: String): Result<BrawlerDataNinja, DataError.NetworkError> {
        return try {
            Result.Success(api.getBrawlerData(name))
        } catch (e: IOException) {
            Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION)
        } catch (e: HttpException) {
            when (e.code()) {
                400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                404 -> Result.Error(DataError.NetworkError.NOT_FOUND)
                429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                else -> Result.Error(DataError.NetworkError.UNKNOWN)
            }
        } catch (e: Exception) {
            Timber.tag("BrawlNinjaRepositoryImpl").e(e)
            Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }
}
