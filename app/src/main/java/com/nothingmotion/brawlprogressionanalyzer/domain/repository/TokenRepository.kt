package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIToken
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result


interface TokenRepository {
    suspend fun getAccessToken(frontEndToken : String) : Result<APIToken,DataError.NetworkError>
    suspend fun validateAccessToken(accessToken: String) : Result<String,DataError.NetworkError>
}