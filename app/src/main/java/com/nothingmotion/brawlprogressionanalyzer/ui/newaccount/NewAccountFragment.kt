package com.nothingmotion.brawlprogressionanalyzer.ui.newaccount

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentNewAccountBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewAccountFragment : Fragment() {
    private var _binding: FragmentNewAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewAccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTextInputListener()
        setupAddButton()
        setupTagHelpLink()
        observeUiState()
    }
    
    private fun setupTextInputListener() {
        binding.playerTagEditText.addTextChangedListener { text: Editable? ->
            // Clear errors when user starts typing
            binding.playerTagLayout.error = null
            if(binding.playerTagEditText.text?.contains("#") == true){
                binding.playerTagEditText.setText(binding.playerTagEditText.text.toString().replace("#",""))
            }
            // Add real-time validation if needed
            text?.toString()?.let { tagText ->
                if (tagText.isNotEmpty()) {
                    val (isValid, _, _) = viewModel.validateTag(tagText)
                    // Set the error icon state based on validation
                    binding.playerTagLayout.isErrorEnabled = !isValid
                }
            }
        }
    }
    
    private fun setupAddButton() {
        binding.addAccountButton.setOnClickListener {
            val tagText = binding.playerTagEditText.text.toString()
            
            // The ViewModel will handle validation and formatting
            viewModel.createAccount(tagText)
        }
    }
    
    private fun setupTagHelpLink() {
        binding.findTagHelp.setOnClickListener {
            // Show a dialog with instructions on how to find tag
            showTagHelpDialog()
        }
    }
    
    private fun showTagHelpDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.tag_help_dialog_title)
            .setMessage(R.string.tag_help_dialog_message)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    handleState(state)
                }
            }
        }
    }
    
    private fun handleState(state: AccountCreationState) {
        when (state) {
            is AccountCreationState.Initial -> {
                // Initial state, just ensure UI is ready
                resetUI()
            }
            
            is AccountCreationState.Loading -> {
                showLoading(true)
            }
            
            is AccountCreationState.Success -> {
                // Account created successfully, navigate back
                showLoading(false)
                showSuccessAndNavigateBack(state.account.account.tag)
            }
            
            is AccountCreationState.Error -> {
                // Show error and reset UI
                showLoading(false)
                showError(state.message)
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingText.text = "Adding Account ${binding.playerTagEditText.text}"
            // Animate button out and show loading
            binding.addAccountButton.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(300)
                .withEndAction {
                    binding.addAccountButton.visibility = View.GONE
                    binding.findTagHelp.visibility = View.GONE
                    binding.loadingStateGroup.visibility = View.VISIBLE
                }
                .start()
                
            // Disable tag help link during loading
            binding.findTagHelp.isEnabled = false
            binding.findTagHelp.alpha = 0.5f
        } else {
            // Hide loading
            binding.loadingStateGroup.visibility = View.GONE
            
            // Animate button back
            binding.addAccountButton.apply {
                visibility = View.VISIBLE
                alpha = 0f
                scaleX = 0.8f
                scaleY = 0.8f
                animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }
            
            // Re-enable tag help link
            binding.findTagHelp.isEnabled = true
            binding.findTagHelp.alpha = 1.0f
        }
        
        // Enable/disable input
        setInputEnabled(!isLoading)
    }
    
    private fun showError(message: String) {
        binding.playerTagLayout.error = message
        
        // Shake the input field to indicate error
        binding.playerTagLayout.apply {
            animate()
                .translationX(-20f)
                .setDuration(100)
                .withEndAction {
                    animate()
                        .translationX(20f)
                        .setDuration(100)
                        .withEndAction {
                            animate()
                                .translationX(0f)
                                .setDuration(100)
                                .start()
                        }
                        .start()
                }
                .start()
        }
    }
    
    private fun showSuccessAndNavigateBack(tag: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.account_added_success, tag),
            Toast.LENGTH_SHORT
        ).show()
        
        // Navigate back to accounts screen
        findNavController().navigate(R.id.action_new_account_to_accounts)
    }
    
    private fun resetUI() {
        binding.loadingStateGroup.visibility = View.GONE
        binding.addAccountButton.visibility = View.VISIBLE
        binding.playerTagLayout.error = null
        setInputEnabled(true)
        binding.findTagHelp.isEnabled = true
        binding.findTagHelp.alpha = 1.0f
    }
    
    private fun setInputEnabled(enabled: Boolean) {
        binding.playerTagEditText.isEnabled = enabled
        binding.addAccountButton.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = NewAccountFragment()
    }
} 