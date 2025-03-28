package com.nothingmotion.brawlprogressionanalyzer.util

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.di.PreferencesManagerModule
import java.util.concurrent.CompletableFuture
import java.util.logging.Handler
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
        android.os.Handler(Looper.getMainLooper()).postAtFrontOfQueue {
            // Use the resource-based approach to avoid activity recreation
            AppCompatDelegate.setDefaultNightMode(
                if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    /**
     * Apply theme to an activity with smooth transition
     * @param activity The activity to apply the theme to
     */
    fun applyThemeWithAnimation(activity: Activity) {
        val darkMode = preferencesManager.darkMode
        val decorView = activity.window.decorView
        
        // Create and start fade out animation
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 200
            addUpdateListener { animator ->
                decorView.alpha = animator.animatedValue as Float
            }
            doOnEnd {
                // Apply theme when faded out
                android.os.Handler(Looper.getMainLooper()).post {
                    AppCompatDelegate.setDefaultNightMode(
                        if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                }
                // Create and start fade in animation
                ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 200
                    addUpdateListener { animator ->
                        decorView.alpha = animator.animatedValue as Float
                    }
                    start()
                }
            }
            start()
        }
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
    
    /**
     * Toggle between light and dark themes with smooth transition
     * @param activity The activity where the theme is being toggled
     */
    fun toggleThemeWithAnimation(activity: Activity): Boolean {
        val newDarkMode = !preferencesManager.darkMode
        preferencesManager.darkMode = newDarkMode
        
        val decorView = activity.window.decorView
        
        // Create and start fade out animation
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 200
            addUpdateListener { animator ->
                decorView.alpha = animator.animatedValue as Float
            }
            doOnEnd {
                // Apply theme when faded out
                AppCompatDelegate.setDefaultNightMode(
                    if (newDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                
                // Create and start fade in animation
                ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 200
                    addUpdateListener { animator ->
                        decorView.alpha = animator.animatedValue as Float
                    }
                    start()
                }
            }
            start()
        }

        return newDarkMode
    }

    fun switchThemeWithResources(activity: Activity) {
        val isDarkMode = preferencesManager.darkMode

        // Dynamically apply theme without recreating activity
        val themeResId = if (isDarkMode)
            com.google.android.material.R.style.Theme_Material3_Light_NoActionBar
        else
            com.google.android.material.R.style.Theme_Material3_Dark_NoActionBar

        activity.setTheme(themeResId)
        activity.window.statusBarColor = MaterialColors.getColor(
            activity.window.decorView,
            com.google.android.material.R.attr.colorPrimaryVariant
        )
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
        
        /**
         * Static helper method for toggling theme with animation when a dependency injector is not available
         */
        fun toggleThemeWithAnimation(activity: Activity): Boolean {
            val preferencesManager = PreferencesManagerModule.getPreferencesManager(activity)
            val newDarkMode = !preferencesManager.darkMode
            preferencesManager.darkMode = newDarkMode

            val decorView = activity.window.decorView
            
            // Create and start fade out animation
            ValueAnimator.ofFloat(1f, 0f).apply {
                duration = 200
                addUpdateListener { animator ->
                    decorView.alpha = animator.animatedValue as Float
                }
                doOnEnd {
                    // Apply theme when faded out
                    AppCompatDelegate.setDefaultNightMode(
                        if (newDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                    
                    // Create and start fade in animation
                    ValueAnimator.ofFloat(0f, 1f).apply {
                        duration = 200
                        addUpdateListener { animator ->
                            decorView.alpha = animator.animatedValue as Float
                        }
                        start()
                    }
                }
                start()
            }

            return newDarkMode
        }
    }
}