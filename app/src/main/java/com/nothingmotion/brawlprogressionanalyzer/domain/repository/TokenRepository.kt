package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result


interface TokenRepository {
    suspend fun getAccessToken(frontEndToken : String) : Result<Any,DataError.NetworkError>
}