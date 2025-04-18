package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PassRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result

interface PassRepository {
    suspend fun getPassFreeTable(token: String): Result<PassRewards,DataError.NetworkError>
    suspend fun getPassPremiumTable(token : String): Result<PassRewards,DataError.NetworkError>
    suspend fun getPassPlusTable(token : String): Result<PassRewards,DataError.NetworkError>
}