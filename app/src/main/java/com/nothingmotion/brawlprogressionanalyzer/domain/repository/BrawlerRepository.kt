package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface BrawlerRepository {
    suspend fun getBrawler(id: Long): Result<BrawlerData,DataError.NetworkError>
    suspend fun getBrawlers(): Flow<Result<List<BrawlerData>,DataError.NetworkError>>
}