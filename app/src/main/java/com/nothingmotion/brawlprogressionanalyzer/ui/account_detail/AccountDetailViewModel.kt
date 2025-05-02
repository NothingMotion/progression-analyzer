package com.nothingmotion.brawlprogressionanalyzer.ui.account_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeAccountRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Brawler
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.RarityData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.toRarityData
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerRepository
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
    private val brawlerDataRepository: BrawlerRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _state = MutableStateFlow<AccountDetailState>(AccountDetailState(loading=true))
    val state: StateFlow<AccountDetailState> = _state

    private var findAccountJob : Job? = null
    private var getTokenJob : Job? = null






    init{

    }

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
                tokenManager.collectAccessToken(it.uuid.toString())
                tokenManager.accessTokenState.collectLatest {state->
                    if(state.loading){
                        _state.update { it.copy(loading = true,error= null) }
                    }
                    else if(state.error != null){
                        _state.update { it.copy(loading = false,error=state.error) }
                    }
                    else {
                        state.success?.let {
                            var account : Account?=null
                            val result = accountRepository.getAccount(accountId.replace("#",""),it)
//                            accountRepository.getAccountHistory(accountId.replace("#",""),it,0,0).collectLatest { result ->
//                                Timber.tag("AccountDetailViewModel").d("Account history: $result")
//                                when(result){
//                                    is Result.Error -> {
//
//                                    }
//                                    is Result.Loading -> {
//
//                                    }
//                                    is Result.Success -> {
//                                        account?.history = result.data.map { it.toPlayer() }
//                                    }
//                                }
//                            }
                            when(result){
                                is Result.Error -> {
                                    when(result.error){
                                        is DataError.NetworkError -> {
                                            Timber.tag("AccountDetailViewModel").e(result.error.name)
                                            _state.update { it.copy(error=result.error.name,loading=false) }
                                        }
                                        is DataError.DatabaseError -> {
                                            Timber.tag("AccountDetailViewModel").e(result.error.name)
                                            _state.update { it.copy(error=result.error.name,loading=false) }
                                        }
                                    }
                                }
                                is Result.Loading -> {
                                    _state.update { it.copy(loading = true,error=null) }
                                }
                                is Result.Success -> {
                                    _state.update {  it.copy(account=result.data,error=null,loading=false)}
































                                    loadBrawlersData()
                                }
                            }

                        }
                    }
                }
            }
        }
    }





    private fun loadBrawlersData(){
        Timber.tag("AccountDetailViewModel").d("Calling loadBrawlersData")
        viewModelScope.launch {
            brawlerDataRepository.getBrawlers().collectLatest {result->
                when(result){
                    is Result.Error -> {}
                    is Result.Loading -> {}
                    is Result.Success -> {
                        _state.update { it.copy(brawlerData = result.data) }
                        sortBrawlersByRarity()
                    }
                }
            }
        }
    }
    fun sortBrawlersByRarity(){
        Timber.tag("AccountDetailViewModel").d("Calling sortBrawlersByRarity")
        val account = _state.value.account ?: return
        val brawlers = account.account.brawlers
        val brawlerData = _state.value.brawlerData
        if(brawlerData.isNullOrEmpty()) return
        Timber.tag("AccountDetailViewModel").d("brawlerData: $brawlerData")
        Timber.tag("AccountDetailViewModel").d("brawlers: ${brawlers.map{it.name}}")
        val sortedBrawlers = mutableMapOf<RarityData,MutableList<Brawler>>()
        brawlers.forEach { brawler->
            Timber.tag("AccountDetailViewModel").d("running or forEach${brawler.name}")
            brawlerData.find { it.name.equals(brawler.name,ignoreCase=true)}?.let{
            Timber.tag("AccountDetailViewModel").d("found brawlerData ${it.name}")
                val rarity= it.rarity.toRarityData()
                sortedBrawlers.getOrPut(rarity){ mutableListOf()}.add(brawler)
                }
            }

        Timber.tag("AccountDetailViewModel").d("Brawlers Rarity: $sortedBrawlers")
        _state.update { it.copy(filteredBrawlerRarity = sortedBrawlers.toList()) }
    }
    fun toggleLoading(){
        _state.update { it.copy(loading = !it.loading) }
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

    fun refreshAccount() {
        viewModelScope.launch {
            val tag =_state.value?.account?.account?.tag ?: return@launch
            if(!accountRepository.isValidCache(tag)) {
                val uuid = preferencesManager.track?.uuid ?: return@launch
                val token = tokenManager.getAccessToken(uuid.toString()) ?: return@launch
                when (val result = accountRepository.refreshAccount(tag,token)) {
                    is Result.Error -> {
                                Timber.tag("AccountDetailViewModel").e(result.error.name)
                                _state.update {
                                    it.copy(
                                        error = result.error.name,
                                        loading = false
                                    )

                            }
                        }
                    is Result.Loading -> {
                        _state.update { it.copy(loading = true, error = null) }
                    }
                    is Result.Success -> {
                        _state.update { it.copy(account= result.data,loading=false,error=null) }
                    }
                }
            }else {
                delay(2000)
                _state.update { it.copy(refreshMessage = "Account is up to date",loading=false) }
                _state.update { it.copy(refreshMessage=null,error=null) }
            }
        }
    }

    data class AccountDetailState(
        val account: Account? = null,
        val brawlerData: List<BrawlerData> = emptyList(),
        val filteredBrawlerRarity: List<Pair<RarityData,List<Brawler>>>?=null,
        val error: String? = null,
        val loading: Boolean = false,
        val refreshMessage: String? = null
    )
} 
