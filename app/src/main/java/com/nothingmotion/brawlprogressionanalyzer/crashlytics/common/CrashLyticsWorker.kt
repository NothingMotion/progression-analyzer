package com.nothingmotion.brawlprogressionanalyzer.crashlytics.common

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.GsonBuilder
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.NotMotRepository
import com.nothingmotion.brawlprogressionanalyzer.util.TokenManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

@HiltWorker
class CrashLyticsWorker @AssistedInject constructor(

    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository:NotMotRepository,
    private val tokenManager: TokenManager,
    private val prefManager: PreferencesManager,): CoroutineWorker(context,params){
    override suspend fun doWork(): Result {

        val crashUri = inputData.getString(CrashLytics.ExceptionHandler.CRASHLYTICS_CRASHREPORT_KEY)
        val crashReport = crashUri?.let {
            File(it).readText().let {
                GsonBuilder().create().fromJson(it, CrashLytics.CrashReport::class.java)
            }
            
        }
        Log.d("CrashLyticsWorker", "${Thread.currentThread().name} - doWork() called")
        Log.d("CrashLyticsWorker", crashUri ?: "null")


//        val tokenState: MutableStateFlow<CrashLyticsTokenState> =
//            MutableStateFlow(CrashLyticsTokenState.Loading)
        Log.d("CrashLyticsWorker","Running CrashLyticsWorker.., ")
        return prefManager.track?.uuid?.let {

            Log.d("CrashLyticsWorker","User UUID was found, continuing for getting access token..")

            tokenManager.getAccessToken(it.toString())?.let {

                Log.d("CrashLyticsWorker","access token was retrieved. going for reporting..")

                withContext(Dispatchers.IO) {
                    when (val result = repository.reportCrash(it, crashReport!!)) {
                        is com.nothingmotion.brawlprogressionanalyzer.domain.model.Result.Error -> {
                            Log.e("CrashLyticsWorker","error while reporting error to api: ${result.error.name}")
                            when (result.error) {
                                DataError.NetworkError.NO_INTERNET_CONNECTION -> {
                                    return@withContext Result.retry()
                                }

                                else -> return@withContext Result.failure()
                            }
                        }

                        is com.nothingmotion.brawlprogressionanalyzer.domain.model.Result.Loading -> {return@withContext Result.failure()}
                        is com.nothingmotion.brawlprogressionanalyzer.domain.model.Result.Success -> {
                            Log.i("CrashLyticsWorker","Successfully reported crashlytics to api!")
                            return@withContext Result.success()
                        }
                    }
                }
            } ?: run {
                Log.e("CrashLyticsWorker","error while getting access token")
                return Result.failure()
            }

        } ?: run {


            Log.e("CrashLyticsWorker","user uuid didn't find")
            return Result.failure()
        }

    }
    sealed class CrashLyticsTokenState {
        data object Loading : CrashLyticsTokenState()

        data class Error(val error: String) : CrashLyticsTokenState()

        data class Success(val token: String) : CrashLyticsTokenState()
    }
}