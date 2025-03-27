package com.nothingmotion.brawlprogressionanalyzer.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.di.PreferencesManagerModule
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for managing theme settings across the app
 */
@Singleton
class ThemeUtils @Inject constructor(private val preferencesManager: PreferencesManager) {

    /**
     * Apply the saved theme from preferences
     */
    fun applyTheme() {
        val darkMode = preferencesManager.darkMode
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    /**
     * Toggle between light and dark themes
     */
    fun toggleTheme(): Boolean {
        val newDarkMode = !preferencesManager.darkMode
        preferencesManager.darkMode = newDarkMode

        AppCompatDelegate.setDefaultNightMode(
            if (newDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        return newDarkMode
    }

    companion object {
        /**
         * Static helper method for applying theme when a dependency injector is not available
         * (e.g., in Application.onCreate)
         */
        fun applyTheme(context: Context) {
            val darkMode = PreferencesManagerModule.getPreferencesManager(context).darkMode
            AppCompatDelegate.setDefaultNightMode(
                if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        /**
         * Static helper method for toggling theme when a dependency injector is not available
         */
        fun toggleTheme(activity: Activity): Boolean {
            val preferencesManager = PreferencesManagerModule.getPreferencesManager(activity)
            val newDarkMode = !preferencesManager.darkMode
            preferencesManager.darkMode = newDarkMode

            AppCompatDelegate.setDefaultNightMode(
                if (newDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            return newDarkMode
        }
    }
}