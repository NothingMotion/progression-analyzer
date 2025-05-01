package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.data.remote.mappers.toBrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.util.DataErrorUtils
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerTableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class BrawlerTableRepositoryImpl @Inject constructor(): BrawlerTableRepository {
    @Inject lateinit var api: ProgressionAnalyzerAPI
    override suspend fun getBrawlerTable(token: String): Flow<Result<List<BrawlerTable>,DataError.NetworkError>> {
        return flow {
            try {
                emit(Result.Success(api.getBrawlerTable("Bearer $token").table.map { it.toBrawlerTable() }))
            }

            catch (e: Exception) {
                Timber.tag("BrawlerTableRepository").e(e, "Error fetching brawler table: $e")
                emit(Result.Error(DataErrorUtils.handleHttpException(e)))
            }
        }

    }
}