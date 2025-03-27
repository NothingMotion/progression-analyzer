package com.nothingmotion.brawlprogressionanalyzer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // For Android 12 and above, this activity won't be used
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            startMainActivity()
            return
        }
        
        setContentView(R.layout.splash_screen)

        // Animate the splash icon
        val splashIcon = findViewById<ImageView>(R.id.splash_icon)
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        splashIcon.startAnimation(fadeIn)

        // Navigate to MainActivity after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            startMainActivity()
        }, 1000) // 1 second delay
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        // Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
} 