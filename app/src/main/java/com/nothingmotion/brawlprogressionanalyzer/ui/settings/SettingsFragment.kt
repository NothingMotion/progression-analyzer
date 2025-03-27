package com.nothingmotion.brawlprogressionanalyzer.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nothingmotion.brawlprogressionanalyzer.MainActivity
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentSettingsBinding
import com.nothingmotion.brawlprogressionanalyzer.model.Language
import com.nothingmotion.brawlprogressionanalyzer.util.LocaleHelper
import com.nothingmotion.brawlprogressionanalyzer.util.ThemeUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    @Inject
    lateinit var themeUtils: ThemeUtils
    
    // Define languages at class level to ensure consistent state
    private val languageOptions by lazy {
        listOf(
            Language.ENGLISH to getString(R.string.language_english),
            Language.PERSIAN to getString(R.string.language_persian)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up dark mode switch with saved preference
        binding.darkModeSwitch.apply {
            isChecked = preferencesManager.darkMode
            setOnCheckedChangeListener { _, isChecked ->
                preferencesManager.darkMode = isChecked
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        // Set up API key save button with secure storage
        binding.apiKeyEditText.setText(preferencesManager.apiKey ?: "")
        binding.saveApiKeyButton.setOnClickListener {
            val apiKey = binding.apiKeyEditText.text.toString()
            preferencesManager.apiKey = apiKey
            Toast.makeText(requireContext(), "API key saved securely", Toast.LENGTH_SHORT).show()
        }

        // Set up notifications switch with saved preference
        binding.notificationsSwitch.apply {
            isChecked = preferencesManager.notificationsEnabled
            setOnCheckedChangeListener { _, isChecked ->
                preferencesManager.notificationsEnabled = isChecked
            }
        }
        binding.showTutorial.setOnClickListener {
            // Get the MainActivity instance and call its showTutorial method
            findNavController().navigate(R.id.navigation_accounts)
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.showTutorial()
                Toast.makeText(requireContext(), getString(R.string.tutorial_started), Toast.LENGTH_SHORT).show()
            }
        }
        
        // Set up language dropdown
        setupLanguageDropdown()
    }
    
    private fun setupLanguageDropdown() {
        // Create a simple array adapter with hardcoded options to ensure stability
        val displayItems = arrayOf(
            getString(R.string.language_english),
            getString(R.string.language_persian)
        )
        
        // Get current language selection
        val currentLanguage = preferencesManager.language ?: Language.ENGLISH
        val currentIndex = when(currentLanguage) {
            Language.ENGLISH -> 0
            Language.PERSIAN -> 1
        }
        
        // Create and set adapter
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            displayItems
        )
        
        // Setup dropdown with adapter
        binding.languageDropdown.apply {
            setAdapter(adapter)
            
            // Set initial selection
            setText(displayItems[currentIndex], false)
            
            // Clear and set listener to avoid multiple registrations
            setOnItemClickListener(null)
            setOnItemClickListener { _, _, position, _ ->
                val selectedLanguage = when(position) {
                    0 -> Language.ENGLISH
                    1 -> Language.PERSIAN
                    else -> Language.ENGLISH
                }
                
                // Only update if language has changed
                if (selectedLanguage != preferencesManager.language) {
                    // Save the selected language preference
                    preferencesManager.language = selectedLanguage
                    
                    // Apply the new locale
                    updateLocale(selectedLanguage)
                    
                    // Use a smooth recreation to apply changes
                    activity?.let { it ->
                        // Add a slight delay to ensure UI is updated properly
                        view?.postDelayed({
                            it.recreate()
                        }, 150)
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Ensure dropdown reflects current settings even after navigation
        binding.languageDropdown.post {
            setupLanguageDropdown()
        }
    }
    
    private fun updateLocale(language: Language) {
        LocaleHelper.setLocale(language)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    companion object {
        fun newInstance() = SettingsFragment()
    }
}