package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIHistory
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    suspend fun getAccount(tag: String,token:String) : Result<Account,DataError>
    suspend fun getAllAccounts(token: String): Flow<Result<List<Account>,DataError.NetworkError>>
    suspend fun getAccountHistory(tag: String,token: String,limit : Int?, offset: Int?) : Flow<Result<List<APIHistory>,DataError.NetworkError>>
    suspend fun refreshAccount(tag: String,token: String) : Result<Account,DataError.NetworkError>
    suspend fun refreshAccounts(token: String): Result<Unit,DataError.NetworkError>

    suspend fun isValidCache(tag: String): Boolean
    suspend fun getCachedAccount(tag: String): Result<Account,DataError.DatabaseError>
    suspend fun getCachedAccounts(): Flow<Result<List<Account>,DataError.DatabaseError>>
    suspend fun insertCachedAccount(account: Account): Result<Unit,DataError.DatabaseError>
    suspend fun insertCachedAccounts(accounts: List<Account>): Result<Unit,DataError.DatabaseError>
    suspend fun deleteCachedAccount(tag: String): Result<Unit,DataError.DatabaseError>
}