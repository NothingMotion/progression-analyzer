package com.nothingmotion.brawlprogressionanalyzer.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Track
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.NotMotRepository
import com.nothingmotion.brawlprogressionanalyzer.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class NotMotViewModel @Inject constructor(
    private val repository: NotMotRepository,
    private val tokenManager: TokenManager,





    private val preferencesManager: PreferencesManager
) : ViewModel() {


    private val _state : MutableStateFlow<TrackState> = MutableStateFlow(TrackState())
    val state get() = _state;
    fun trackUser(data: Track){
        Log.d("NotMotViewModel","tracking user..")
        viewModelScope.launch {
            preferencesManager.track?.let{

                _state.update { it.copy(loading=true,error=null) }
                val token = tokenManager.collectAccessToken(it.uuid.toString())
                tokenManager.accessTokenState.collectLatest{state->
                    if (state.loading){
                        _state.update { it.copy(loading=true,error=null) }
                    }
                    else if(state.error != null){
                        _state.update { it.copy(loading=false,error=state.error) }
                    }
                    state.success?.let {

                        when (val result = repository.trackUser(it,data)){
                            is Result.Error -> _state.update {  it.copy(error = result.error.name,loading=false)}
                            is Result.Loading -> _state.update { it.copy(loading=true) }
                            is Result.Success -> _state.update { it.copy(success=true,error= null, loading = false) }
                        }
                    }
                }

            }
        }
    }
}

data class TrackState(
    var loading: Boolean = false,
    var error: String? = null,
    val success: Boolean = false
)