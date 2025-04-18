package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeAccountRepository
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Date
import kotlin.random.Random

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: FakeAccountRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

//    // Exposed accounts as StateFlow from repository
//    val accounts: StateFlow<List<Account>> = accountRepository.accounts
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = emptyList()
//        )
        
    // Unified accounts state that includes loading, error, and data
    private val _accountsState = MutableStateFlow(AccountsState(isLoading = true))
    val accountsState = _accountsState.asStateFlow()

    // Sorting preference
    private val _sortOrder = MutableStateFlow(SortOrder.TROPHIES_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    init {
        // Load sort order from preferences if we wanted to
        // loadSortOrderPreference()
        
        // Initial load of accounts
//        refreshAccounts()
        loadAccounts()
    }
    
     fun loadAccounts(){
        viewModelScope.launch {
            try {
                accountRepository.accounts.collectLatest {accounts->
                    // %50 to throw error
                    if (Random.nextInt(0, 5) == 0) {
                        throw Exception("Failed")
//                        _accountsState.update{it.copy(isLoading = false, error = "Failed to load accounts")}

                    }
                    _accountsState.update { it.copy(isLoading = true,error= null) }
                    delay(2000)
                    _accountsState.update { it.copy(accounts = accounts,isLoading= false, error = null) }
                }
            }catch (e: Exception){
                Log.d("AccountsViewModel","error while fetching accounts ${e.message}")
                _accountsState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load accounts")
                }
            }
        }
    }
    /**
     * Refresh accounts from the repository
     */
    fun refreshAccounts() {
        viewModelScope.launch {
            try {
                // Set loading state
                _accountsState.update { it.copy(isLoading = true, error = null) }
                
                // Load accounts from repository
                accountRepository.refreshAccounts()
                
                // Update state with loaded accounts
                _accountsState.update { 
                    it.copy(accounts = it.accounts, isLoading = false, error = null)
                }
            } catch (e: Exception) {
                // Update state with error
                _accountsState.update { 
                    it.copy(isLoading = false, error = e.message ?: "Failed to load accounts") 
                }
            }
        }
    }
    fun getAccount(tag: String): Account? {
        var account: Account? = null
        viewModelScope.launch {

            account = accountRepository.getAccount(tag)
        }
        return account;
    }
    /**
     * Add a new account
     */
    fun addAccount(account: Account) {
        viewModelScope.launch {
            try {
                accountRepository.addAccount(account)
                // After adding, refresh the list
                refreshAccounts()
            } catch (e: Exception) {
                _accountsState.update { 
                    it.copy(error = "Failed to add account: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * Delete an account
     */
    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            try {
                accountRepository.deleteAccount(accountId)
                _accountsState.value.accounts = _accountsState.value.accounts.filter { it.account.tag !=  accountId}
                // After deleting, we don't need to refresh manually if the repository emits updates
            } catch (e: Exception) {
                _accountsState.update { 
                    it.copy(error = "Failed to delete account: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * Update account tag
     */
    fun updateAccountTag(accountId: String, newTag: String) {
        viewModelScope.launch {
            try {
                val account = accountRepository.getAccount(accountId)
                account?.let {
                    val updatedPlayer = it.account.copy(
                        tag = newTag
                    )
                    val updatedAccount = it.copy(
                        account = updatedPlayer,
                        updatedAt = Date()
                    )
                    accountRepository.updateAccount(updatedAccount)
                }
            } catch (e: Exception) {
                _accountsState.update { 
                    it.copy(error = "Failed to update account: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * Change sort order of accounts
     */
    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
        // Save preference
        viewModelScope.launch {
            // preferencesManager.accountSortOrder = sortOrder.name
        }
    }
    
    /**
     * Load saved sort order preference
     */
    private fun loadSortOrderPreference() {
        /* Example implementation for future:
        viewModelScope.launch {
            val savedSortOrder = preferencesManager.accountSortOrder
            if (savedSortOrder != null) {
                try {
                    _sortOrder.value = SortOrder.valueOf(savedSortOrder)
                } catch (e: IllegalArgumentException) {
                    // Use default if saved value can't be parsed
                    _sortOrder.value = SortOrder.TROPHIES_DESC
                }
            }
        }
        */
    }
    
    /**
     * Represents sort options for account list
     */
    enum class SortOrder {
        TROPHIES_DESC,
        LEVEL_DESC,
        NAME_ASC,
        LAST_UPDATED_DESC
    }
} 