package com.nothingmotion.brawlprogressionanalyzer.util

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.animation.doOnEnd
import com.google.android.material.color.MaterialColors
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
        // Post at front of queue to ensure theme is applied immediately without blocking
        android.os.Handler(Looper.getMainLooper()).post {
            AppCompatDelegate.setDefaultNightMode(
                if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    // Cache for animation objects to avoid repeated instantiation
    private val animators = mutableMapOf<View, ValueAnimator>()

    /**
     * Apply theme to an activity with smooth transition
     * @param activity The activity to apply the theme to
     */
    fun applyThemeWithAnimation(activity: Activity) {
        val darkMode = preferencesManager.darkMode
        val decorView = activity.window.decorView

        // Cancel any ongoing animation for this view
        animators[decorView]?.cancel()
        
        // Create and reuse fade out animation
        val fadeOut = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 150
            addUpdateListener { animator ->
                decorView.alpha = animator.animatedValue as Float
            }
            doOnEnd {
                // Apply theme when faded out - reduced to a single operation
                AppCompatDelegate.setDefaultNightMode(
                    if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                
                // Create and reuse fade in animation
                val fadeIn = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 150
                    addUpdateListener { animator ->
                        decorView.alpha = animator.animatedValue as Float
                    }
                    doOnEnd {
                        // Clean up reference when animation completes
                        animators.remove(decorView)
                    }
                }
                animators[decorView] = fadeIn
                fadeIn.start()
            }
        }
        animators[decorView] = fadeOut
        fadeOut.start()
    }

    /**
     * Toggle between light and dark themes
     */
    fun toggleTheme(): Boolean {
        val newDarkMode = !preferencesManager.darkMode
        preferencesManager.darkMode = newDarkMode

        // Apply theme immediately on the main thread
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
        
        // Cancel any ongoing animation for this view
        animators[decorView]?.cancel()
        
        // Create fade out animation
        val fadeOut = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 150
            addUpdateListener { animator ->
                decorView.alpha = animator.animatedValue as Float
            }
            doOnEnd {
                // Apply theme directly without handler to reduce overhead
                AppCompatDelegate.setDefaultNightMode(
                    if (newDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                
                // Create fade in animation
                val fadeIn = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 150
                    addUpdateListener { animator ->
                        decorView.alpha = animator.animatedValue as Float
                    }
                    doOnEnd {
                        // Clean up reference when animation completes
                        animators.remove(decorView)
                    }
                }
                animators[decorView] = fadeIn
                fadeIn.start()
            }
        }
        animators[decorView] = fadeOut
        fadeOut.start()

        return newDarkMode
    }

    // Cache theme resource IDs
    private val lightTheme = com.google.android.material.R.style.Theme_Material3_Light_NoActionBar
    private val darkTheme = com.google.android.material.R.style.Theme_Material3_Dark_NoActionBar

    fun switchThemeWithResources(activity: Activity) {
        val isDarkMode = preferencesManager.darkMode

        // Apply cached theme IDs
        activity.setTheme(if (isDarkMode) darkTheme else lightTheme)
        
        // Get color once and reuse
        val statusBarColor = MaterialColors.getColor(
            activity.window.decorView,
            com.google.android.material.R.attr.colorPrimaryVariant
        )
        activity.window.statusBarColor = statusBarColor
    }

    companion object {
        // Static animation cache
        private val staticAnimators = mutableMapOf<View, ValueAnimator>()
        
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
            
            // Cancel any ongoing animation for this view
            staticAnimators[decorView]?.cancel()
            
            // Create fade out animation with shorter duration
            val fadeOut = ValueAnimator.ofFloat(1f, 0f).apply {
                duration = 150
                addUpdateListener { animator ->
                    decorView.alpha = animator.animatedValue as Float
                }
                doOnEnd {
                    // Apply theme directly
                    AppCompatDelegate.setDefaultNightMode(
                        if (newDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                    
                    // Create fade in animation with shorter duration
                    val fadeIn = ValueAnimator.ofFloat(0f, 1f).apply {
                        duration = 150
                        addUpdateListener { animator ->
                            decorView.alpha = animator.animatedValue as Float
                        }
                        doOnEnd {
                            // Clean up reference when done
                            staticAnimators.remove(decorView)
                        }
                    }
                    staticAnimators[decorView] = fadeIn
                    fadeIn.start()
                }
            }
            staticAnimators[decorView] = fadeOut
            fadeOut.start()

            return newDarkMode
        }
    }
}