package com.nothingmotion.brawlprogressionanalyzer

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
//import androidx.navigation.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.doOnPreDraw
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.core.view.updatePadding
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.model.Language
import com.nothingmotion.brawlprogressionanalyzer.util.LanguageDialogHelper
import com.nothingmotion.brawlprogressionanalyzer.util.LocaleHelper
import com.nothingmotion.brawlprogressionanalyzer.util.ThemeUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.nothingmotion.brawlprogressionanalyzer.databinding.ActivityMainBinding
import com.nothingmotion.brawlprogressionanalyzer.tutorial.TutorialManager
import com.nothingmotion.brawlprogressionanalyzer.ui.accounts.AccountsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isReady = false
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    
    @Inject
    lateinit var themeUtils: ThemeUtils

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Handle the splash screen transition
            // Keep the splash screen visible until our app is ready
            splashScreen.setKeepOnScreenCondition { !isReady }

            // Set up the splash screen exit animation
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                // Create your custom animation here
                val slideUp = ObjectAnimator.ofFloat(
                    splashScreenView.view,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenView.view.height.toFloat()
                ).apply {
                    interpolator = AnticipateInterpolator()
                    duration = 500L
                }

                // Start the animation and remove the splash screen once it's done
                slideUp.doOnEnd { splashScreenView.remove() }
                slideUp.start()
            }
        }
        // Simulate some loading work
        setupApp()

        // Apply theme from preferences before setting content view
        themeUtils.applyThemeWithAnimation(this)
        // Apply saved language first if available
        preferencesManager.language?.let { savedLanguage ->
            applyLanguage(savedLanguage)
        }

        if (preferencesManager.language == null){

                LanguageDialogHelper.showLanguageSelectionDialog(this@MainActivity,preferencesManager)
        }
        enableEdgeToEdge()
        
        // Postpone transitions until the layout is ready
        postponeEnterTransition()
        
        // Set up navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
//        // Listen for navigation changes and ensure language is consistently applied
//        navController.addOnDestinationChangedListener { _, _, _ ->
//            // Reapply language settings when destination changes
//            preferencesManager.language?.let { savedLanguage ->
//                applyLanguage(savedLanguage)
//            }
//        }
        
        // Set up bottom navigation with smooth transitions
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setupWithNavController(navController)
//

        // Handle edge-to-edge display with proper insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply padding only to the top
            view.updatePadding(top = insets.top)

            // Apply padding to the bottom navigation
            bottomNavigation.updatePadding(bottom = insets.bottom)

            windowInsets
        }

        setupNavigation()
        
        // Show tutorial if it's the first time
        if (TutorialManager.shouldShowTutorial(this)) {
            lifecycleScope.launch {

                PreferencesManager.isPickedLanguage.collectLatest {isPicked->
                    if(isPicked)
                        showTutorial()
                }
            }
        }
    }

    private fun setupApp() {
        // Do your app initialization here
        // For example, load initial data, set up viewmodels, etc.
        
        // Once everything is ready, set isReady to true
        isReady = true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun applyLanguage(language: Language) {
        LocaleHelper.setLocale(language)
    }

    private fun setupNavigation() {
        // Implementation of setupNavigation method
    }


    fun showTutorial() {
        val tutorialSteps = listOf(
            TutorialManager.TutorialStep(
                R.id.bottom_navigation,
                "Navigate through different sections using the bottom navigation bar",
                16
            ),
            TutorialManager.TutorialStep(
                R.id.fab_add_account,
                "Tap here to add a new Brawl Stars account",
                16
            ),
            TutorialManager.TutorialStep(
                R.id.bottom_navigation,
                "View all your accounts and their progress here",
                24
            ),
            TutorialManager.TutorialStep(R.id.appBarLayout,"Click here to see wiki and other options",16),
            TutorialManager.TutorialStep(
                R.id.navigation_settings,
                "Access settings to customize the app, change language, and theme",
                16
            )
        )

        TutorialManager(this).initTutorial(tutorialSteps) {
            // Tutorial completed
            TutorialManager.markTutorialAsShown(this)
        }
    }
}