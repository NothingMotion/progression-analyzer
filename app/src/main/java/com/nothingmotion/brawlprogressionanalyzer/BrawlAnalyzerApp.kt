package com.nothingmotion.brawlprogressionanalyzer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.github.mikephil.charting.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.di.PreferencesManagerModule
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for Brawl Progression Analyzer
 * Initializes dependencies and global components
 */
@HiltAndroidApp
class BrawlAnalyzerApp : Application() {
    
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

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
} 