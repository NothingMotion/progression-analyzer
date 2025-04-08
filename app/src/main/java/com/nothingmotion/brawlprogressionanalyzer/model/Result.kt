package com.nothingmotion.brawlprogressionanalyzer.model
import com.nothingmotion.brawlprogressionanalyzer.model.Error as Error

typealias RootError = Error
sealed interface Result<out D, out E: RootError> {
    data class Success<out D, out E: RootError>(val data: D): Result<D, E>
    data class Loading<out D, out E: RootError>(val isLoading: Boolean): Result<D, E>
    data class Error<out D, out E: RootError>(val error: E): Result<D, E>
}