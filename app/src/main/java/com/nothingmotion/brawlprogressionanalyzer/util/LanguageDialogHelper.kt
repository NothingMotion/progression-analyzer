package com.nothingmotion.brawlprogressionanalyzer.util

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.databinding.DialogLanguageSelectionBinding
import com.nothingmotion.brawlprogressionanalyzer.model.Language

/**
 * Helper class to manage language selection dialogs and locale changes
 */
object LanguageDialogHelper {

    /**
     * Shows the language selection dialog for initial setup
     * @param activity The activity context
     * @param preferencesManager The preferences manager to save selection
     */
    fun showLanguageSelectionDialog(
        activity: Activity,
        preferencesManager: PreferencesManager
    ) {
        val binding = DialogLanguageSelectionBinding.inflate(LayoutInflater.from(activity))
        
        // Set default selection to English if none selected
        binding.radioEnglish.isChecked = true
        
        // Set current selection if available
        preferencesManager.language?.let { currentLanguage ->
            when (currentLanguage) {
                Language.ENGLISH -> binding.radioEnglish.isChecked = true
                Language.PERSIAN -> binding.radioPersian.isChecked = true
            }
        }
        
        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.language_selection_dialog_title)
            .setMessage(R.string.language_selection_dialog_message)
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val selectedLanguage = when {
                    binding.radioEnglish.isChecked -> Language.ENGLISH
                    binding.radioPersian.isChecked -> Language.PERSIAN
                    else -> Language.ENGLISH
                }
                
                // Save selection and update locale
                preferencesManager.language = selectedLanguage
                LocaleHelper.setLocale(selectedLanguage)
                
                // Recreate activity to apply changes
                activity.recreate()
            }
            .create()
            
        dialog.show()
    }
} 