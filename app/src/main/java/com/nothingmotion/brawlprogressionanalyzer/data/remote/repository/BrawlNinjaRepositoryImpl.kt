package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.BrawlNinjaApi
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.util.DataErrorUtils
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlNinjaRepository
import timber.log.Timber
import javax.inject.Inject

class BrawlNinjaRepositoryImpl @Inject constructor() : BrawlNinjaRepository {
    @Inject
    lateinit var api: BrawlNinjaApi

    override suspend fun getBrawlerData(name: String): Result<BrawlerDataNinja, DataError.NetworkError> {
        return try {
            Result.Success(api.getBrawlerData(name))
        } catch (e: Exception) {
            Timber.tag("BrawlNinjaRepositoryImpl").e(e)
            Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }
}
