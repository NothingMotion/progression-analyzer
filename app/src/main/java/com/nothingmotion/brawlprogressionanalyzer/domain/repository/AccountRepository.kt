package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.History
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    suspend fun getAccount(tag: String) : Result<Account,DataError.NetworkError>
    suspend fun getAllAccounts(): Flow<Result<List<Account>,DataError.NetworkError>>
    suspend fun getAccountHistory(tag: String,limit : Int?, offset: Int?) : Flow<Result<List<History>,DataError.NetworkError>>
    suspend fun refreshAccount(tag: String) : Result<Account,DataError.NetworkError>
}