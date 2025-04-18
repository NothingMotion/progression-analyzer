package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.UpgradeTable

interface UpgradeTableRepository {
    suspend fun getUpgradeTable(token: String) : Result<UpgradeTable,DataError.NetworkError>
}