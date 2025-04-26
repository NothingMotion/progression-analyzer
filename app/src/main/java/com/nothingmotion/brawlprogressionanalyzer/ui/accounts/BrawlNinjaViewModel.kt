package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import android.content.Context
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.BrawlAnalyzerApp
import com.nothingmotion.brawlprogressionanalyzer.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlNinjaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Random
import javax.inject.Inject

@HiltViewModel
class BrawlNinjaViewModel @Inject constructor(private val repository: BrawlNinjaRepository) :
    ViewModel() {
    private val _state = MutableStateFlow<BrawlNinjaState>(BrawlNinjaState.Loading)
    val state get() = _state;


//    private val _cachedBrawlers = LruCache<String, BrawlerDataNinja>(100)

    fun getBrawlerFromCache(context:Context,name: String): BrawlerDataNinja? {
        val app = context as? BrawlAnalyzerApp
        val lowerCaseName = name.lowercase()
        val cachedData = app?.brawlerDataNinjaCache?.get(lowerCaseName)
        Timber.tag("BrawlNinjaViewModel")
            .d("getBrawlerFromCache: $lowerCaseName -> ${cachedData != null}")
        return cachedData
    }

    fun getBrawler(context: Context,name: String) {
        Timber.tag("BrawlNinjaViewModel").d("getBrawler: $name")

        // Always update to loading state first
        _state.update { BrawlNinjaState.Loading }

        // For testing purposes, we'll use a more controlled approach
        // Only generate random errors if the name doesn't start with 'retry_'

        // TODO: Remove this random error generation in production
        if (BuildConfig.DEBUG) {

            val shouldGenerateError = !name.startsWith("retry_") && Random().nextInt(2) == 0
            if (shouldGenerateError) {
                Timber.tag("BrawlNinjaViewModel").d("Generating random error for: $name")
                _state.update { BrawlNinjaState.Error("Error fetching brawler data") }
                return
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            val parsedName = name
                .lowercase()
                .replace("retry_", "")
                .replace("&", "_")
                .replace(" ", "_")
                .replace("'", "")
                .replace("`", "")
            when (val result = repository.getBrawlerData(
                parsedName
            )) {

                is Result.Error -> {
                    _state.update { BrawlNinjaState.Error(result.error.name) }
                    Timber.tag("BrawlNinjaViewModel").e("getBrawler: $result $parsedName")
                }

                is Result.Loading -> {
                    _state.update { BrawlNinjaState.Loading }
                    Timber.tag("BrawlNinjaViewModel").d("getBrawler: $result")
                }

                is Result.Success -> {
                    val data = result.data
                    Timber.tag("BrawlNinjaViewModel").d("getBrawler: $data")
                    if (data != null) {
                        // Ensure we use lowercase for consistent cache keys
                        val app = context as? BrawlAnalyzerApp
                        app?.brawlerDataNinjaCache?.put(data.name.lowercase(), data)
                        Timber.tag("BrawlNinjaViewModel")
                            .d("Cached brawler: ${name.lowercase()} -> $data")
                        _state.update { BrawlNinjaState.Success(data) }
                    } else {
                        _state.update { BrawlNinjaState.Error("No data found") }
                    }
                }
            }
        }
    }

    sealed class BrawlNinjaState {
        data object Loading : BrawlNinjaState()
        data class Error(val message: String) : BrawlNinjaState()
        data class Success(val data: BrawlerDataNinja) :
            BrawlNinjaState() // Replace Any with your actual data type
    }
}
