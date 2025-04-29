package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.crashlytics.common.CrashLytics
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeAccountRepository
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.data.db.ApplicationDatabase
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.AccountEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.PlayerEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.ProgressEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.ProgressType
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Progress
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import com.nothingmotion.brawlprogressionanalyzer.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val fakeAccountRepository: FakeAccountRepository,
    private val preferencesManager: PreferencesManager,
    private val tokenManager: TokenManager,
    private val crashLytics: CrashLytics.ExceptionHandler,
    private val db: ApplicationDatabase
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
//        loadFakeAccounts()
    }

    fun loadFakeAccounts() {
        viewModelScope.launch {
            fakeAccountRepository.accounts.collectLatest { accounts ->
                delay(5000)
//                _accountsState.update { it.copy(error="An internal error occured",isLoading= false) }
                _accountsState.update {
                    it.copy(
                        accounts = accounts,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun loadAccounts() {
        viewModelScope.launch {

                preferencesManager.track?.let { track ->

                    _accountsState.update { it.copy(isLoading = true, error = null) }
                    tokenManager.collectAccessToken(track.uuid.toString())
                    tokenManager.accessTokenState.collectLatest { state ->

                        Timber.tag("AccountsViewModel").d(state.error, state.success)

                        if (state.loading) {
                            _accountsState.update { it.copy(isLoading = true, error = null) }

                        } else if (state.error != null) {

                            Timber.tag("AccountsViewModel").e(state.error)
                            _accountsState.update {
                                it.copy(
                                    isLoading = false,
                                    error = state.error
                                )
                            }

                        } else {
                            state.success?.let {

                                accountRepository.getAllAccounts(it)
//                            .apply { delay(2000) }
                                    .collect { result ->
                                        when (result) {
                                            is Result.Error -> _accountsState.update {
                                                it.copy(
                                                    isLoading = false,
                                                    error = result.error.name
                                                )
                                            }

                                            is Result.Loading -> _accountsState.update {
                                                it.copy(
                                                    isLoading = true,
                                                    error = null
                                                )
                                            }

                                            is Result.Success -> {
                                                result.data.forEach { account ->




                                                }
                                                _accountsState.update {
                                                    it.copy(
                                                        accounts = result.data,
                                                        error = null,
                                                        isLoading = false
                                                    )
                                                }
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }?: run {
                    _accountsState.update {
                        it.copy(
                            error = "Restart Application to fix",
                            isLoading = false
                        )
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
//                accountRepository.refreshAccounts()

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

//            account = accountRepository.getAccount(tag)
        }
        return account;
    }

    /**
     * Add a new account
     */
    fun addAccount(account: Account) {
//        viewModelScope.launch {
//            try {
//                accountRepository.addAccount(account)
//                // After adding, refresh the list
//                refreshAccounts()
//            } catch (e: Exception) {
//                _accountsState.update {
//                    it.copy(error = "Failed to add account: ${e.message}")
//                }
//            }
//        }
    }

    /**
     * Delete an account
     */
    fun deleteAccount(accountId: String) {
        // Set loading state
        _accountsState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Delete account from repository
                accountRepository.deleteCachedAccount(accountId)

                // Update state to remove deleted account
                _accountsState.update {
                    it.copy(
                        accounts = it.accounts.filter { account -> account.account.tag != accountId },
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                // Update state with error
                _accountsState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to delete account")
                }
            }
        }
        // Delete account from repository
//        viewModelScope.launch {
//            try {
//                accountRepository.deleteAccount(accountId)
//                _accountsState.value.accounts = _accountsState.value.accounts.filter { it.account.tag !=  accountId}
//                // After deleting, we don't need to refresh manually if the repository emits updates
//            } catch (e: Exception) {
//                _accountsState.update {
//                    it.copy(error = "Failed to delete account: ${e.message}")
//                }
//            }
//        }
    }

    /**
     * Update account tag
     */
    fun updateAccountTag(accountId: String, newTag: String) {
//        viewModelScope.launch {
//            try {
//                val account = accountRepository.getAccount(accountId)
//                account?.let {
//                    val updatedPlayer = it.account.copy(
//                        tag = newTag
//                    )
//                    val updatedAccount = it.copy(
//                        account = updatedPlayer,
//                        updatedAt = Date()
//                    )
//                    accountRepository.updateAccount(updatedAccount)
//                }
//            } catch (e: Exception) {
//                _accountsState.update {
//                    it.copy(error = "Failed to update account: ${e.message}")
//                }
//            }
//        }
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
