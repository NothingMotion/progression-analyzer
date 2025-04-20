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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val fakeAccountRepository: FakeAccountRepository,
    private val preferencesManager: PreferencesManager,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _state = MutableStateFlow<AccountDetailState>(AccountDetailState(loading=true))
    val state: StateFlow<AccountDetailState> = _state

    private var findAccountJob : Job? = null
    private var getTokenJob : Job? = null








    fun getFakeAccount(accountId: String){
        viewModelScope.launch {
            delay(5000)
            _state.update {  it.copy(account= fakeAccountRepository.getAccount(accountId), loading = false,error= null)}
        }
    }
    /**
     * Load the account details from the repository
     */
    fun getAccount(accountId: String) {
        findAccountJob = viewModelScope.launch {
            preferencesManager.track?.let {
                _state.update { it.copy(loading=true,error=null) }
                tokenManager.getAccessToken(it.uuid.toString())
                tokenManager.accessTokenState.collectLatest {state->
                    if(state.loading){
                        _state.update { it.copy(loading = true,error= null) }
                    }
                    else if(state.error != null){
                        _state.update { it.copy(loading = false,error=state.error) }
                    }
                    else {
                        state.success?.let {
                            val result = accountRepository.getAccount(accountId.replace("#",""),it)
                            when(result){
                                is Result.Error -> {
                                    Timber.tag("AccountDetailViewModel").e(result.error.name)
                                    _state.update { it.copy(error=result.error.name,loading=false) }
                                }
                                is Result.Loading -> {
                                    _state.update { it.copy(loading = true,error=null) }
                                }
                                is Result.Success ->_state.update {  it.copy(account=result.data,error=null,loading=false)}
                            }

                        }
                    }
                }
            }
        }
    }
    


    fun stop(){
        findAccountJob?.cancel()
        Timber.tag("AccountDetailViewModel").d("Finding Account job was cancelled.")
    }


    /**
     * Update an account
     */
    fun updateAccount(account: Account) {
        viewModelScope.launch {
//            accountRepository.updateAccount(account)
//            _state.value = account
        }
    }
    
    /**
     * Update an account tag
     */
    fun updateAccountTag(accountId: String, newTag: String) {
        viewModelScope.launch {
//            val account = accountRepository.getAccount(accountId)
            state?.let {
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
            _state.value?.let { account ->
//                accountRepository.deleteAccount(account.account.id)
//                _state.value = null
            }
        }
    }
    data class AccountDetailState(
        val account: Account? = null,
        val error: String? = null,
        val loading: Boolean = false
    )
} 