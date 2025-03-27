package com.nothingmotion.brawlprogressionanalyzer.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nothingmotion.brawlprogressionanalyzer.model.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages app preferences with support for both regular and secure encrypted preferences.
 */
@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {
    // Regular preferences for non-sensitive data like UI settings
    private val standardPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // Secure encrypted preferences for sensitive data (API keys, tokens, etc)
    private val securePrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Theme preferences
    var darkMode: Boolean
        get() = standardPrefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = standardPrefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    // Notification preferences
    var notificationsEnabled: Boolean
        get() = standardPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = standardPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    // Language preferences
    var language: Language?
        get() {
            val languageValue = standardPrefs.getString(LANGUAGE_KEY, null)
            _isPickedLanguage.value = languageValue != null
            return languageValue?.let { Language.valueOf(it) }
        }
        set(value) = standardPrefs.edit().putString(LANGUAGE_KEY, value?.name).apply()

    // Secure API key storage
    var apiKey: String?
        get() = securePrefs.getString(KEY_API_KEY, null)
        set(value) = securePrefs.edit().putString(KEY_API_KEY, value).apply()

    companion object {
        private const val SECURE_PREFS_FILE_NAME = "secure_preferences"

        // Regular preference keys
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

        private const val LANGUAGE_KEY = "language"

        // Secure preference keys
        private const val KEY_API_KEY = "api_key"

        private val _isPickedLanguage  = MutableStateFlow<Boolean> (false)
        val isPickedLanguage get() = PreferencesManager._isPickedLanguage.asStateFlow()
    }
}