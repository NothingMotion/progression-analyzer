package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result

interface AccountRepository {
    suspend fun getAccount(tag: String) : Result<Account,DataError.NetworkError>
    suspend fun getAllAccounts(): List<Account>
}