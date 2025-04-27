package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PassRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlNinjaRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerTableRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.PassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FutureProgressViewModel @Inject constructor(
    private val brawlerTableRepository: BrawlerTableRepository,
    private val upgradeTableRepository: BrawlerTableRepository,
    private val passRepository: PassRepository,
    private val starrDropRepository: BrawlerTableRepository,
    private val brawlNinjaRepository: BrawlNinjaRepository
) : ViewModel() {

    private val _state = MutableStateFlow<FutureProgressState>(FutureProgressState())

    val state get()= _state

    init {

    }
    fun getBrawlerTable(token: String){
        viewModelScope.launch {
            brawlerTableRepository.getBrawlerTable(token).collect{result ->
                when(result){
                    is Result.Error -> {}
                    is Result.Loading -> {}
                    is Result.Success -> {
                        Timber.tag("FutureProgressViewModel").d("brawler table: ${result.data}")
                        _state.update { it.copy(brawlerTable = result.data) }
                    }
                }

            }
        }
    }
}


data class FutureProgressState (
        val brawlerTable: List<BrawlerTable> = emptyList(),
        val upgradeTable: BrawlerTable?=null,
        val passFreeRewards: PassRewards?=null,
        val passPremiumRewards: PassRewards?=null,
        val passPlusRewards: PassRewards?=null,
        val starrDropRewards: List<StarrDropRewards> = emptyList(),
        val brawlersDataNinja: List<BrawlerDataNinja> = emptyList()
)