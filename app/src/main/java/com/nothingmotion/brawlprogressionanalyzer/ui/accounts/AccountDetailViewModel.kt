package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeAccountRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import com.nothingmotion.brawlprogressionanalyzer.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val preferencesManager: PreferencesManager,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account
    
    /**
     * Load the account details from the repository
     */
    fun getAccount(accountId: String) {
        viewModelScope.launch {
            preferencesManager.track?.let {
                val token = tokenManager.getAccessToken(it.uuid.toString())
//                token?.let {
//                    val result = accountRepository.getAccount(accountId.replace("#",""),token)
//                    when(result){
//                        is Result.Error -> Timber.tag("AccountDetailViewModel").e(result.error.name)
//                        is Result.Loading -> TODO()
//                        is Result.Success ->_account.value = result.data
//                    }
//
//                }
            }
        }
    }
    
    /**
     * Update an account
     */
    fun updateAccount(account: Account) {
        viewModelScope.launch {
//            accountRepository.updateAccount(account)
            _account.value = account
        }
    }
    
    /**
     * Update an account tag
     */
    fun updateAccountTag(accountId: String, newTag: String) {
        viewModelScope.launch {
//            val account = accountRepository.getAccount(accountId)
            account?.let {
//                val updatedPlayer = it.account.copy(tag = newTag)
//                val updatedAccount = it.copy(
//                    account = updatedPlayer,
//                    updatedAt = Date()
//                )
//                accountRepository.updateAccount(updatedAccount)
//                _account.value = updatedAccount
            }
        }
    }
    
    /**
     * Delete the current account
     */
    fun deleteAccount() {
        viewModelScope.launch {
            _account.value?.let { account ->
//                accountRepository.deleteAccount(account.account.id)
                _account.value = null
            }
        }
    }
} 