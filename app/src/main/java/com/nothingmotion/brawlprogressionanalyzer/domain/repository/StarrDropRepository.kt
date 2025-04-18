package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropRewards
import kotlinx.coroutines.flow.Flow

interface StarrDropRepository {
    suspend fun getStarrDropRewards(token : String) : Flow< Result<List<StarrDropRewards>,DataError.NetworkError>>
}