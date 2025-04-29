package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.db.ApplicationDatabase
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.BrawlDataNinjaEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.toDatabase
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.toDomain
import com.nothingmotion.brawlprogressionanalyzer.data.remote.BrawlNinjaApi
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.util.DataErrorUtils
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlNinjaRepository
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class BrawlNinjaRepositoryImpl @Inject constructor() : BrawlNinjaRepository {
    @Inject
    lateinit var api: BrawlNinjaApi


    @Inject
    lateinit var db: ApplicationDatabase
    override suspend fun getBrawlerData(name: String): Result<BrawlerDataNinja, DataError.NetworkError> {
        return try {
            db.brawlDataNinjaDao().getBrawlDataNinja(name)?.let {
                val cacheAgeMillis = Date().time - it.createdAt.time
                val cacheExpirationMillis = 24 * 60 * 60 * 1000 // 24 hours in milliseconds
                val isCacheValid = cacheAgeMillis < cacheExpirationMillis
                
                if (isCacheValid) {
                    Timber.tag("BrawlNinjaRepositoryImpl").d("Using cached brawler data - cache age: ${cacheAgeMillis/1000/60} minutes")
                    return Result.Success(it.toDomain())
                } else {
                    Timber.tag("BrawlNinjaRepositoryImpl").d("Cache expired - age: ${cacheAgeMillis/1000/60} minutes, fetching fresh data")
                    null
                }
            } ?: run {
                Timber.tag("BrawlNinjaRepositoryImpl").d("No cached data found for $name, fetching from API")
                val brawlerData = api.getBrawlerData(name)
                db.brawlDataNinjaDao().insertBrawlDataNinja(brawlerData.toDatabase())
                Result.Success(brawlerData)
            }
        } catch (e: Exception) {
            Timber.tag("BrawlNinjaRepositoryImpl").e(e)
            Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }
}
