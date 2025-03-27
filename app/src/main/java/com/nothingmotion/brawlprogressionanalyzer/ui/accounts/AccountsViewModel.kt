package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.data.FakeAccountRepository
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.model.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Date

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: FakeAccountRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // Exposed accounts as StateFlow from repository
    val accounts: StateFlow<List<Account>> = accountRepository.accounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Sorting preference
    private val _sortOrder = MutableStateFlow(SortOrder.TROPHIES_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    init {
        // Load sort order from preferences if we wanted to
        // loadSortOrderPreference()
    }
    
    /**
     * Add a new account
     */
    fun addAccount(account: Account) {
        viewModelScope.launch {
            accountRepository.addAccount(account)
        }
    }
    
    /**
     * Delete an account
     */
    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            accountRepository.deleteAccount(accountId)
        }
    }
    
    /**
     * Update account tag
     */
    fun updateAccountTag(accountId: String, newTag: String) {
        viewModelScope.launch {
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