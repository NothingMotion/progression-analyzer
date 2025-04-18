package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface BrawlerTableRepository {
    suspend fun getBrawlerTable(token: String) : Flow<Result<List<BrawlerTable>, DataError.NetworkError>>
}