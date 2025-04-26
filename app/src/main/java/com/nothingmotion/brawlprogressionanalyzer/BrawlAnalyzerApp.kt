package com.nothingmotion.brawlprogressionanalyzer

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.github.mikephil.charting.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.di.PreferencesManagerModule
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import androidx.collection.LruCache
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import android.content.Context
import androidx.work.WorkerParameters
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nothingmotion.brawlprogressionanalyzer.crashlytics.common.CrashLytics
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerDataNinja
import com.nothingmotion.brawlprogressionanalyzer.crashlytics.common.CrashLyticsWorkerFactory
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.util.GlobalExceptionHandling
import javax.inject.Inject

/**
 * Application class for Brawl Progression Analyzer
 * Initializes dependencies and global components
 */
@HiltAndroidApp
class BrawlAnalyzerApp : Application(), Configuration.Provider {
    
    // Cache for player icon URLs (Long = icon ID, String? = image URL or null if not available)
    val iconCache = LruCache<Long, String>(100) // Cache up to 100 icons
    val brawlerDataCache = LruCache<Long, String>(200)
    val brawlerDataNinjaCache = LruCache<String,BrawlerDataNinja>(100)

    @Inject lateinit var hiltWorkerFactory: HiltWorkerFactory
    @Inject lateinit var crashLyticsWorkerFactory: CrashLyticsWorkerFactory

    @Inject lateinit var preferencesManager: PreferencesManager
    override fun onCreate() {
        super.onCreate()

        GlobalExceptionHandling.setup(this.applicationContext)
        CrashLytics.ExceptionHandler.setup(this.applicationContext,preferencesManager)
        AndroidThreeTen.init(this)

        // Initialize Timber for logging
//        if (BuildConfig.DEBUG) {
//            Log.d("BrawlerAnalyzerApp","Setting up Timber library")
//            Timber.plant(Timber.DebugTree())
//        }

        val prefsManager  = PreferencesManagerModule.getPreferencesManager(this)
        // Apply the saved theme before Hilt is initialized (using static accessor)
        val darkMode = prefsManager.darkMode
        // Apply language before Hilt is initialized
        val language =  prefsManager.language

        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun getWorkManagerConfiguration(): Configuration {

        Timber.tag("BrawlAnalyzerApp").d("getWorkManagerConfiguration() called")

        
        return Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .setMinimumLoggingLevel(Log.VERBOSE)
            .build()
    }
}