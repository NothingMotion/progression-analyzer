package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.data.repository.FakeAccountRepository
import com.nothingmotion.brawlprogressionanalyzer.model.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountRepository: FakeAccountRepository
) : ViewModel() {
    
    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account
    
    /**
     * Load the account details from the repository
     */
    fun getAccount(accountId: String) {
        viewModelScope.launch {
            val result = accountRepository.getAccount(accountId)
            _account.value = result
        }
    }
    
    /**
     * Update an account
     */
    fun updateAccount(account: Account) {
        viewModelScope.launch {
            accountRepository.updateAccount(account)
            _account.value = account
        }
    }
    
    /**
     * Update an account tag
     */
    fun updateAccountTag(accountId: String, newTag: String) {
        viewModelScope.launch {
            val account = accountRepository.getAccount(accountId)
            account?.let {
                val updatedPlayer = it.account.copy(tag = newTag)
                val updatedAccount = it.copy(
                    account = updatedPlayer,
                    updatedAt = Date()
                )
                accountRepository.updateAccount(updatedAccount)
                _account.value = updatedAccount
            }
        }
    }
    
    /**
     * Delete the current account
     */
    fun deleteAccount() {
        viewModelScope.launch {
            _account.value?.let { account ->
                accountRepository.deleteAccount(account.account.id)
                _account.value = null
            }
        }
    }
} 