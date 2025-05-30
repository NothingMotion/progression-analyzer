package com.nothingmotion.brawlprogressionanalyzer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Language
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Track
import com.nothingmotion.brawlprogressionanalyzer.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    companion object {
        private const val SPLASH_DELAY = 1000L // 1 second delay
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        
        // Skip splash for Android 12+ which uses the system splash screen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            processLanguageAndNavigate()
            return
        }
        
        // Animate the splash icon
        findViewById<ImageView>(R.id.nothing)?.let { splashIcon ->
            val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            splashIcon.startAnimation(fadeIn)
        }
        
        // Process language settings and navigate after animation
        processLanguageAndNavigate()
    }
    
    private fun processLanguageAndNavigate() {
        val (versionString,version) = getAppVersionInfo()
        if (preferencesManager.track == null) {
            preferencesManager.track = Track(version= version,versionString= versionString)
        }else {
            if (version > preferencesManager.track!!.version!!){
                preferencesManager.track =  preferencesManager.track!!.copy(version= version,versionString= versionString)
            }
        }
        // Apply language if saved, otherwise go directly to main activity
        preferencesManager.language?.let { savedLanguage ->
            applyLanguage(savedLanguage)
        } ?: navigateToMainWithDelay()
    }
    private fun getAppVersionInfo(): Pair<String, Long> {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName ?: "unknown"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            versionName to versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown" to -1
        }
    }
    private fun applyLanguage(language: Language) {
        lifecycleScope.launch(Dispatchers.Default) {
            val currentLocale = LocaleHelper.getCurrentLocale(applicationContext).language
            val targetLocale = LocaleHelper.getLocale(language)
            
            if (currentLocale != targetLocale) {
                // Language needs to change
                withContext(Dispatchers.Main) {
                    LocaleHelper.setLocale(language)
                    // Navigate after setting the locale
                    navigateToMainWithDelay()
                }
            } else {
                // Language already set correctly
                withContext(Dispatchers.Main) {
                    navigateToMainWithDelay()
                }
            }
        }
    }
    
    private fun navigateToMainWithDelay() {
        // For Android 12+, we don't need a delay as we're already handling this in onCreate
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            startMainActivity()
            return
        }
        
        // Add a delay for the splash animation on older versions
        Handler(Looper.getMainLooper()).postDelayed({
            startMainActivity()
        }, SPLASH_DELAY)
    }
    
    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        // Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
} 