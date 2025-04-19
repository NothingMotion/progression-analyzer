package com.nothingmotion.brawlprogressionanalyzer

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Language
import com.nothingmotion.brawlprogressionanalyzer.util.AssetUtils
import com.nothingmotion.brawlprogressionanalyzer.util.LanguageDialogHelper
import com.nothingmotion.brawlprogressionanalyzer.util.LocaleHelper
import com.nothingmotion.brawlprogressionanalyzer.util.ThemeUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.nothingmotion.brawlprogressionanalyzer.databinding.ActivityMainBinding
import com.nothingmotion.brawlprogressionanalyzer.domain.model.toJson
import com.nothingmotion.brawlprogressionanalyzer.ui.NotMotViewModel
import com.nothingmotion.brawlprogressionanalyzer.ui.tutorial.TutorialManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date

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


    val notmotViewModel: NotMotViewModel by viewModels()
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
        setupTimber()
        // Simulate some loading work
        setupApp()
        lifecycleScope.launch {

            notmotViewModel.state.collectLatest {
                Timber.tag("MainActivty")
                    .d("track state is: " + it.success + " and error is: " + it.error)
            }
        }

        // Apply theme from preferences before setting content view
        themeUtils.applyThemeWithAnimation(this)
        // Apply saved language first if available
        preferencesManager.language?.let { savedLanguage ->
//            applyLanguage(savedLanguage)
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
//        navController.navigate(R.id.accordionViewUsageExampleFragment)

        // Show tutorial if it's the first time
        if (TutorialManager.shouldShowTutorial(this)) {
            lifecycleScope.launch {

                PreferencesManager.isPickedLanguage.collectLatest {isPicked->
                    if(isPicked)
                        showTutorial()
                }
            }
        }
        
        // Example of loading an image from assets (uncomment when you have images in assets)
        // loadImageFromAssets()
    }
    private  fun setupTimber(){
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
    private fun setupApp() {
        // Do your app initialization here
        // For example, load initial data, set up viewmodels, etc.
        
        // Once everything is ready, set isReady to true
        preferencesManager.track?.let{it->
            var track = it;
            track = track.copy(date = Date())
            Timber.tag("MainActivity").d(track.toJson())
            notmotViewModel.trackUser(track)
        }
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
    
    /**
     * Example function to demonstrate loading an image from assets
     * This can be used in any activity or fragment to load assets
     */
    private fun loadImageFromAssets() {
        // Example: load an image called "example.png" from assets folder
        // Make sure you have this image in your assets folder
        lifecycleScope.launch {
            // You can use either the sync or async method depending on your needs
            // Synchronous loading (not recommended for large images on main thread)
            // val bitmap = AssetUtils.loadImageFromAssets(this@MainActivity, "example.png")
            
            // Asynchronous loading (preferred for most cases)
            val bitmap = AssetUtils.loadImageAsync(this@MainActivity, "example.png")
            
            // Use the bitmap in an ImageView or other component
            bitmap?.let {
                // Example: If you have an ImageView in your layout with id "imageFromAssets"
                // findViewById<ImageView>(R.id.imageFromAssets).setImageBitmap(it)
                
                // Or with view binding if available
                // binding.imageFromAssets.setImageBitmap(it)
            }
        }
    }
    
    /**
     * Lists all files in the specified assets directory and handles them
     */
    private fun listAssetImages(directory: String = "") {
        val assetFiles = AssetUtils.listAssetFiles(this, directory)
        
        // Do something with the list of files
        for (file in assetFiles) {
            // Process each file as needed
            // For example, you might want to load all images in a directory
            if (file.endsWith(".png") || file.endsWith(".jpg") || file.endsWith(".jpeg")) {
                // This is an image file
                val imagePath = if (directory.isEmpty()) file else "$directory/$file"
                // Load and use the image
                // AssetUtils.loadImageFromAssets(this, imagePath)
            }
        }
    }
}