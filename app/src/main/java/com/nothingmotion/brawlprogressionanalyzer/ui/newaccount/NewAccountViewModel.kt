package com.nothingmotion.brawlprogressionanalyzer.ui.newaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeAccountRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Brawler
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Player
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Progress
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import com.nothingmotion.brawlprogressionanalyzer.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NewAccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val preferencesManager: PreferencesManager,
    private val tokenManager: TokenManager,
) : ViewModel() {

    // State to represent the account creation process
    private val _uiState = MutableStateFlow<AccountCreationState>(AccountCreationState.Initial)
    val uiState: StateFlow<AccountCreationState> = _uiState.asStateFlow()


    /**
     * Validate a player tag
     * @param tag The tag to validate
     * @return Triple of (isValid, formattedTag, errorMessage)
     */
    fun validateTag(tag: String): Triple<Boolean, String, String?> {
        // Trim the tag and ensure it starts with #
        val formattedTag = formatTag(tag)
        
        // Empty tag check
        if (formattedTag.isEmpty()) {
            return Triple(false, formattedTag, "Tag cannot be empty")
        }
        
        // Basic validation - should start with # and have 8-9 alphanumeric characters
        val regex = Regex("^#[0-9A-Za-z]{3,}$")
        val isValid = regex.matches(formattedTag)
        
        return if (isValid) {
            Triple(true, formattedTag, null)
        } else {
            Triple(false, formattedTag, "Invalid tag format. Should be like #2YQ9VRLJ")
        }
    }
    
    /**
     * Format the tag by ensuring it starts with #
     */
    private fun formatTag(tag: String): String {
        val trimmedTag = tag.trim()
        return if (trimmedTag.isEmpty()) {
            ""
        } else if (trimmedTag.startsWith("#")) {
            trimmedTag.uppercase()
        } else {
            "#${trimmedTag.uppercase()}"
        }
    }

    /**
     * Create a new account with the given tag
     */
    fun createAccount(tag: String) {
        val (isValid, formattedTag, errorMessage) = validateTag(tag)
        
        if (!isValid) {
            _uiState.value = AccountCreationState.Error(errorMessage ?: "Invalid tag")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = AccountCreationState.Loading

                // Check if account already exists
//                when(val result =accountRepository.getCachedAccount(formattedTag)){
//                    is Result.Error -> {
//                        when(result.error){
//                            else -> {
//                                _uiState.value =
//                                    AccountCreationState.Error("Database error: ${result.error.name}")
//                                return@launch
//                            }
//                        }
//                    }
//                    is Result.Loading -> {}
//                    is Result.Success -> {
//                        if (result.data != null) {
//                            _uiState.value = AccountCreationState.Error("Account with this tag already exists")
//                            return@launch
//                        }
//                    }
//                }
//                if (existingAccount != null) {
//                    _uiState.value = AccountCreationState.Error("Account with this tag already exists")
//                    return@launch
//                }

                // In a real app, you would fetch player data from API here
                // For now, simulate API call with delay
//                kotlinx.coroutines.delay(5000)

                preferencesManager.track?.let { track ->

                    _uiState.update { AccountCreationState.Loading }
                    tokenManager.collectAccessToken(track.uuid.toString())
                    tokenManager.accessTokenState.collectLatest { state ->

                        Timber.tag("AccountsViewModel").d(state.error, state.success)

                        if (state.loading) {
                            _uiState.update { AccountCreationState.Loading }

                        } else if (state.error != null) {

                            Timber.tag("AccountsViewModel").e(state.error)
                            _uiState.update { AccountCreationState.Error(state.error) }

                        }
                        else {
                            state.success?.let { token ->
                                when(val result = accountRepository.getAccount(tag,token)){
                                    is Result.Error -> {
                                        when(result.error){
                                            is DataError.NetworkError -> {
                                                Timber.tag("AccountCreationViewModel").e(result.error.name)
                                                _uiState.update { AccountCreationState.Error(result.error.name) }
                                            }
                                            is DataError.DatabaseError -> {
                                                Timber.tag("AccountCreationViewModel").e(result.error.name)
                                                _uiState.update { AccountCreationState.Error(result.error.name) }
                                            }
                                        }
                                    }
                                    is Result.Loading -> {
                                        _uiState.update { AccountCreationState.Loading }
                                    }
                                    is Result.Success -> {

                                        if (result.data != null) {
                                            _uiState.value = AccountCreationState.Success(result.data)
                                        } else {
                                            _uiState.value = AccountCreationState.Error("Failed to fetch account data")
                                        }
                                    }
                                }
                                // Fetch account data from API
//                                val account = fetchAccountData(formattedTag, token)
                            }
                        }
                    }
                // Create a new starter account
//                val newAccount = createNewAccountFromTag(formattedTag)
//                addAccount(newAccount)

//                _uiState.value = AccountCreationState.Success(newAccount)
            }
            } catch (e: Exception) {
                _uiState.value = AccountCreationState.Error("Failed to create account: ${e.message}")
            }
        }
    }
    
    /**
     * Add an account to the repository
     */
    private suspend fun addAccount(account: Account) {
        try {
//            accountRepository.addAccount(account)
            throw Exception("This is demo exception")
        } catch (e: Exception) {
            throw Exception("Failed to add account: ${e.message}")
        }
    }

    /**
     * Create a new account object from a tag
     * In a real app, this would use API data
     */
    private fun createNewAccountFromTag(tag: String): Account {
        val id = UUID.randomUUID().toString()
        val now = Date()
        
        // Create basic player with starter values
        val player = Player(
//            id = id,
            name = "New Player", // This would come from API
            tag = tag,
            trophies = 100,
            highestTrophies = 100,
            level = 1,
            brawlers = listOf(
                // Start with just one basic brawler
                Brawler(
                    id = 1,
                    name = "Shelly",
                    trophies = 0,
                    highestTrophies = 0,
                    rank = 1,
                    power = 1,
                    gears = null,
                    starPowers = null,
                    gadgets = null
                )
            ),
            createdAt = now
        )
        
        // Create starter progress
        val progress = Progress(
            coins = 0,
            powerPoints = 0,
            credits = 0,
            gears = 0,
            starPowers = 0,
            gadgets = 0,
            brawlers = 1,
            averageBrawlerPower = 1,
            averageBrawlerTrophies = 0,
            isBoughtPass = false,
            isBoughtPassPlus = false,
            isBoughtRankedPass = false,
            duration = now
        )
        
        return Account(
            account = player,
            history = null,
            previousProgresses = null,
            currentProgress = progress,
            futureProgresses = null,
            createdAt = now,
            updatedAt = now
        )
    }

    /**
     * Reset the state to initial
     */
    fun resetState() {
        _uiState.value = AccountCreationState.Initial
    }
}

/**
 * Represents the different states of account creation
 */
sealed class AccountCreationState {
    // Initial state
    object Initial : AccountCreationState()
    
    // Loading state while creating account
    object Loading : AccountCreationState()
    
    // Success state with the created account
    data class Success(val account: Account) : AccountCreationState()
    
    // Error state with error message
    data class Error(val message: String) : AccountCreationState()
} 