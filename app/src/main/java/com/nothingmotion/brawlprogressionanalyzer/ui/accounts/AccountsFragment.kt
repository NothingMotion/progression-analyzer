package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.button.MaterialButton
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentAccountsBinding
import com.nothingmotion.brawlprogressionanalyzer.databinding.DialogEditTagBinding
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.view.animation.OvershootInterpolator
import android.content.res.Configuration
import android.util.Log
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerRepository
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AccountsFragment : Fragment() {
    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AccountsViewModel by viewModels()
    private lateinit var accountsAdapter: AccountsAdapter
    @Inject
    lateinit var preferencesManager : PreferencesManager
    @Inject
    lateinit var brawlerRepository: BrawlerRepository
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupAddButton()
        setUpRetryButton()
        observeAccounts()
    }

    private fun setUpRetryButton() {
        binding.retryButton.setOnClickListener {
            hideErrorWithAnimation()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset animations when fragment resumes
        if (::accountsAdapter.isInitialized) {
            accountsAdapter.resetAnimationState()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Disable animations on configuration change
        if (::accountsAdapter.isInitialized) {
            accountsAdapter.disableAnimations()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            // Set up the toolbar menu
            inflateMenu(R.menu.menu_accounts)
            
            // Handle menu item clicks
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_wiki -> {
                        handleWikiClick()
                        true
                    }
                    R.id.action_about_us -> {
                        handleAboutUsClick()
                        true
                    }
                    R.id.action_telegram -> {
                        handleTelegramClick()
                        true
                    }
                    R.id.action_settings -> {
                        handleSettingsClick()
                        true
                    }
                    else -> false
                }
            }
        }
    }
    
    private fun handleWikiClick() {
        // Open wiki in browser (example URL)
        val wikiUrl = "https://brawlstars.fandom.com/wiki/Brawl_Stars_Wiki"
//        openUrl(wikiUrl)
    }
    
    private fun handleAboutUsClick() {
        // Open about us page in browser (example URL)
        val aboutUsUrl = "https://github.com/yourorganization/brawlprogressionanalyzer"
//        openUrl(aboutUsUrl)
    }
    
    private fun handleTelegramClick() {
        try {
            // Try to open Telegram app
            val telegramIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/brawlprogressionanalyzer"))
            telegramIntent.setPackage("org.telegram.messenger")
            startActivity(telegramIntent)
        } catch (e: Exception) {
            // If Telegram is not installed, open in browser
            val webIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/brawlprogressionanalyzer"))
            startActivity(webIntent)
        }
    }
    
    private fun handleSettingsClick() {
        // Navigate to settings screen
        val navOptions= NavOptions.Builder().setPopUpTo(R.id.navigation_accounts,true).build()
        findNavController().navigate(R.id.action_accounts_to_settings,null,navOptions)
    }

    private fun setupRecyclerView() {
        accountsAdapter = AccountsAdapter(
            onItemClicked = { account ->
                handleAccountClick(account)
            },
            onItemLongClicked = { account ->
                showShareDialog(account)
                true
            },
            preferencesManager,
            brawlerRepository
        )
        
        binding.accountsRecyclerView.apply {
            adapter = accountsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            
            // Set up swipe actions
            setupSwipeActions(this)
        }
    }
    
    private fun setupSwipeActions(recyclerView: RecyclerView) {
        val editSwipeCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            private val editBackground = ColorDrawable(
                ContextCompat.getColor(requireContext(), R.color.edit_background)
            )
            private val editIcon = ContextCompat.getDrawable(
                requireContext(), R.drawable.ic_copy
            )
            private val editIconMargin = resources.getDimensionPixelSize(R.dimen.swipe_icon_margin)
            private val paint = Paint().apply {
                color = Color.WHITE
                textSize = resources.getDimensionPixelSize(R.dimen.swipe_text_size).toFloat()
                textAlign = Paint.Align.LEFT
            }
            
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val account = accountsAdapter.currentList[position]
                
                // Reset the swipe state by notifying the adapter
                accountsAdapter.notifyItemChanged(position)
                
                // Handle edit action
//                handleEditAccount(account)
                showShareDialog(account)
            }
            
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.height
                
                // Draw background
                editBackground.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                editBackground.draw(c)
                
                // Draw icon
                val iconTop = itemView.top + (itemHeight - editIcon!!.intrinsicHeight) / 2
                val iconMargin = (itemHeight - editIcon.intrinsicHeight) / 2
                val iconLeft = itemView.right - iconMargin - editIcon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                val iconBottom = iconTop + editIcon.intrinsicHeight
                
                editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                editIcon.draw(c)
                
                // Draw text
                c.drawText(
                    "Copy",
                    itemView.right - editIconMargin * 3f - editIcon.intrinsicWidth,
                    itemView.top + itemHeight / 2f + paint.textSize / 3,
                    paint
                )
                
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        
        val deleteSwipeCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT
        ) {
            private val deleteBackground = ColorDrawable(
                ContextCompat.getColor(requireContext(), R.color.delete_background)
            )
            private val deleteIcon = ContextCompat.getDrawable(
                requireContext(), R.drawable.ic_delete
            )
            private val deleteIconMargin = resources.getDimensionPixelSize(R.dimen.swipe_icon_margin)
            private val paint = Paint().apply {
                color = Color.WHITE
                textSize = resources.getDimensionPixelSize(R.dimen.swipe_text_size).toFloat()
                textAlign = Paint.Align.RIGHT
            }
            
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val account = accountsAdapter.currentList[position]
                
                // Reset the swipe state by notifying the adapter
                accountsAdapter.notifyItemChanged(position)
                
                // Show confirmation dialog
                showDeleteConfirmationDialog(account)
            }
            
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.height
                
                // Draw background
                deleteBackground.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
                )
                deleteBackground.draw(c)
                
                // Draw icon
                val iconTop = itemView.top + (itemHeight - deleteIcon!!.intrinsicHeight) / 2
                val iconMargin = (itemHeight - deleteIcon.intrinsicHeight) / 2
                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + deleteIcon.intrinsicWidth
                val iconBottom = iconTop + deleteIcon.intrinsicHeight
                
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon.draw(c)
                
                // Draw text
                c.drawText(
                    "Delete",
                    itemView.left + deleteIconMargin * 3f + deleteIcon.intrinsicWidth,
                    itemView.top + itemHeight / 2f + paint.textSize / 3,
                    paint
                )
                
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        
        // Attach edit swipe helper
        ItemTouchHelper(editSwipeCallback).attachToRecyclerView(recyclerView)
        
        // Attach delete swipe helper
        ItemTouchHelper(deleteSwipeCallback).attachToRecyclerView(recyclerView)
    }
    
    private fun handleEditAccount(account: Account) {
        val dialogBinding = DialogEditTagBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_tag_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save, null) // Set up later to prevent auto-dismiss
            .setNegativeButton(R.string.cancel, null)
            .create()
        
        val editText = dialogBinding.tagEditText
        val currentTag = if (account.account.tag.startsWith("#")) account.account.tag.substring(1) else account.account.tag
        editText.setText(currentTag)
        editText.setSelection(currentTag.length)
        
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val tag = editText.text.toString().trim()
                if (tag.isEmpty()) {
                    dialogBinding.tagInputLayout.error = getString(R.string.tag_empty_error)
                    return@setOnClickListener
                }
                
                val formattedTag = if (tag.startsWith("#")) tag else "#$tag"
                if (!isValidTag(formattedTag)) {
                    dialogBinding.tagInputLayout.error = getString(R.string.tag_invalid_format)
                    return@setOnClickListener
                }
                
                // Valid tag, update the account
                viewModel.updateAccountTag(account.account.tag, formattedTag)
                dialog.dismiss()
                
                // Show success message
                Snackbar.make(
                    binding.root,
                    getString(R.string.tag_updated),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        
        dialog.show()
    }
    
    private fun isValidTag(tag: String): Boolean {
        // Basic validation - should start with # and have 8-9 alphanumeric characters
        val regex = Regex("^#[0-9A-Z]{8,9}$")
        return regex.matches(tag)
    }
    
    private fun showDeleteConfirmationDialog(account: Account) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete ${account.account.name}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAccount(account.account.tag)
                Snackbar.make(
                    binding.root,
                    "Account deleted",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setupAddButton() {
        binding.fabAddAccount.setOnClickListener {
            // Navigate to add account screen
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.navigation_accounts, true).build()
            findNavController().navigate(R.id.action_accounts_to_new_account, null, navOptions)
        }
    }
    
    private fun observeAccounts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Use the accountsState flow instead of separate flows
                viewModel.accountsState.collectLatest { state ->
                    Timber.tag("AccountsFragment").d("collecting account state..")
                    // Handle loading state
                    if (state.isLoading) {
                        showLoadingState()
                    } else if (state.error != null) {
                        Timber.tag("AccountsFragment").e(state.error)
                        // Handle error state
                        showErrorState(state.error)
                    } else {
                        // Data loaded successfully
                        accountsAdapter.submitList(state.accounts)
                        hideLoadingWithAnimation(state.accounts)
                    }
                }
            }
        }
    }
    
    private fun showLoadingState() {
        binding.loadingAccountsGroup.apply {
            // Only animate if not already visible
            if (visibility != View.VISIBLE) {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            }
        }
        binding.emptyStateGroup.visibility = View.GONE
        binding.accountsRecyclerView.visibility = View.GONE
        binding.errorStateGroup?.visibility = View.GONE
    }
    private fun hideErrorWithAnimation(){
        binding.errorStateGroup.animate()
            .translationY(-50f)
            .alpha(0f)
            .setDuration(300)
            .withEndAction{
                binding.errorStateGroup.visibility = View.GONE
                viewModel.loadAccounts()

            }
            .start()
    }
    private fun hideLoadingWithAnimation(accounts: List<Account>) {
        // Fade out loading view
        binding.loadingAccountsGroup.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                binding.loadingAccountsGroup.visibility = View.GONE
                
                // Update visibility based on accounts data
                if (accounts.isEmpty()) {
                    // Show empty state with animation
                    binding.emptyStateGroup.apply {
                        alpha = 0f
                        visibility = View.VISIBLE
                        animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start()
                    }
                } else {
                    // Reset adapter animation state for fresh animations
                    accountsAdapter.resetAnimationState()
                    
                    // Show recycler view with animation
                    binding.accountsRecyclerView.apply {
                        alpha = 0f
                        visibility = View.VISIBLE
                        animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start()
                    }
                    binding.emptyStateGroup.visibility = View.GONE
                }
                
                // Hide error state if showing
                binding.errorStateGroup?.visibility = View.GONE
            }
            .start()
    }
    
    private fun showErrorState(error: String) {
        Log.d("AccountsFragment","error occured")
        // Fade out loading and content
        val fadeOutDuration = 200L
        
        // Create a list of animations to run in parallel
        val animations = mutableListOf<Animator>()
        
        if (binding.loadingAccountsGroup.visibility == View.VISIBLE) {
            val loadingFadeOut = ObjectAnimator.ofFloat(binding.loadingAccountsGroup, "alpha", 1f, 0f)
            loadingFadeOut.duration = fadeOutDuration
            animations.add(loadingFadeOut)
        }
        
        if (binding.accountsRecyclerView.visibility == View.VISIBLE) {
            val contentFadeOut = ObjectAnimator.ofFloat(binding.accountsRecyclerView, "alpha", 1f, 0f)
            contentFadeOut.duration = fadeOutDuration
            animations.add(contentFadeOut)
        }
        
        if (binding.emptyStateGroup.visibility == View.VISIBLE) {
            val emptyStateFadeOut = ObjectAnimator.ofFloat(binding.emptyStateGroup, "alpha", 1f, 0f)
            emptyStateFadeOut.duration = fadeOutDuration
            animations.add(emptyStateFadeOut)
        }
        
        // Create AnimatorSet to play all fade out animations together
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animations)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Hide views that were faded out
                binding.loadingAccountsGroup.visibility = View.GONE
                binding.accountsRecyclerView.visibility = View.GONE
                binding.emptyStateGroup.visibility = View.GONE
                
                // Show and animate error view
                binding.errorStateGroup.apply {
                    alpha = 1f
                    visibility = View.VISIBLE

                    // Add subtle entrance animation for error state
                    binding.errorIcon?.apply {
                        translationY = -50f
                        alpha = 0f
                        animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(400)
                            .setInterpolator(OvershootInterpolator(1.2f))
                            .start()
                    }

                    binding.errorTitle?.apply {
                        translationY = -30f
                        alpha = 0f
                        animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setStartDelay(100)
                            .setDuration(400)
                            .start()
                    }

                    binding.errorMessageText?.apply {
                        text = error
                        translationY = -20f
                        alpha = 0f
                        animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setStartDelay(200)
                            .setDuration(400)
                            .start()
                    }

                    binding.retryButton?.apply {
                        scaleX = 0.8f
                        scaleY = 0.8f
                        alpha = 0f
                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setStartDelay(300)
                            .setDuration(400)
                            .setInterpolator(OvershootInterpolator())
                            .start()
                    }
                }
                
                // If no error view exists, show a Snackbar
                if (binding.errorStateGroup == null) {
                    Snackbar.make(
                        binding.root,
                        error,
                        Snackbar.LENGTH_LONG
                    ).setAction("Retry") {
                        viewModel.loadAccounts()
                    }.show()
                }
            }
        })
        
        // Start the animations
        animatorSet.start()
    }
    
    private fun updateEmptyState(accounts: List<Account>) {
        binding.loadingAccountsGroup.visibility = View.GONE
        
        if (accounts.isEmpty()) {
            binding.emptyStateGroup.visibility = View.VISIBLE
            binding.accountsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateGroup.visibility = View.GONE
            binding.accountsRecyclerView.visibility = View.VISIBLE
        }
        
        // Hide error state if showing
        binding.errorStateGroup?.visibility = View.GONE
    }
    
    private fun handleAccountClick(account: Account) {
        // Navigate to account details using the global action
        val bundle = Bundle().apply {
            putString("accountId", account.account.tag)
        }
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.navigation_accounts,false).build()
        findNavController().navigate(R.id.action_global_to_account_detail, bundle,navOptions)
    }

    private fun showShareDialog(account: Account) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_share_account, null)
        
        // Set account details in the dialog
        val accountDetails = dialogView.findViewById<TextView>(R.id.account_details)
        val shareText = buildShareText(account)
        accountDetails.text = shareText
        
        // Setup copy button
        val copyButton = dialogView.findViewById<MaterialButton>(R.id.copy_button)
        copyButton.setOnClickListener {
            copyToClipboard(shareText)
        }
        
        // Setup share button
        val shareButton = dialogView.findViewById<MaterialButton>(R.id.share_button)
        shareButton.setOnClickListener {
            shareAccountInfo(shareText)
        }
        
        // Ensure consistent button appearance
        configureButtonTextEllipsize(copyButton)
        configureButtonTextEllipsize(shareButton)
        
        // Show dialog
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    /**
     * Configure button text to use ellipsis in case of overflow
     */
    private fun configureButtonTextEllipsize(button: MaterialButton) {
        button.maxLines = 1
        button.ellipsize = android.text.TextUtils.TruncateAt.END
    }
    
    private fun buildShareText(account: Account): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val lastUpdated = dateFormat.format(account.updatedAt)
        
        return buildString {
            append("${account.account.name} (${account.account.tag})\n")
            append("Level: ${account.account.level}\n")
            append("Trophies: ${account.account.trophies}\n")
            append("Brawlers: ${account.account.brawlers.size} (${account.account.brawlers.count { it.power >= 11 }} maxed)\n")
            append("Last updated: $lastUpdated\n")
            append("\nShared from Brawl Progression Analyzer")
        }
    }
    
    private fun copyToClipboard(text: String) {
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Account Info", text)
        clipboardManager.setPrimaryClip(clipData)
        
        // Show confirmation
        Snackbar.make(
            binding.root,
            R.string.copied_to_clipboard,
            Snackbar.LENGTH_SHORT
        ).show()
    }
    
    private fun shareAccountInfo(text: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_account)))
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AccountsFragment()
    }
} 