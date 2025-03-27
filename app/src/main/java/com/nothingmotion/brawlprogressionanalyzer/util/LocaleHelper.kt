package com.nothingmotion.brawlprogressionanalyzer.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.nothingmotion.brawlprogressionanalyzer.model.Language
import java.util.Locale

/**
 * Helper class for handling locale-related functionality
 */
object LocaleHelper {

    /**
     * Updates the application locale based on the selected language
     * @param language The language to set
     */
    fun setLocale(language: Language) {
        val locale = when (language) {
            Language.ENGLISH -> "en"
            Language.PERSIAN -> "fa"
        }
        
        val localeList = LocaleListCompat.forLanguageTags(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
//        AppCompatDelegate
    }
    
    /**
     * Check if the current locale is RTL (Right-to-Left)
     * @param context The context to check
     * @return true if the current locale is RTL
     */
    fun isRtl(context: Context): Boolean {
        val config = context.resources.configuration
        return config.layoutDirection == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL
    }
    
    /**
     * Get the current locale from the configuration
     * @param context The context
     * @return The current locale
     */
    fun getCurrentLocale(context: Context): Locale {
        val config = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            config.locale
        }
    }
    
    /**
     * Get the language name for a given locale code
     * @param languageCode The language code (e.g., "en", "fa")
     * @return The display name of the language
     */
    fun getLanguageName(languageCode: String): String {
        return Locale(languageCode).displayLanguage
    }
} 