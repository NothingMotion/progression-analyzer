package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import com.nothingmotion.brawlprogressionanalyzer.model.Account

data class AccountsState
    (
    val accounts: List<Account> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false

)
