package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result

interface BrawlNinjaRepository {
    suspend fun getBrawlerData(name: String): Result<BrawlerDataNinja, DataError.NetworkError>
}
