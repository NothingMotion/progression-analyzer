package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account

data class AccountsState
    (
    var accounts: List<Account> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false

)
