package com.nothingmotion.brawlprogressionanalyzer.ui.future_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.card.MaterialCardView
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentFutureProgressBinding
import com.nothingmotion.brawlprogressionanalyzer.domain.model.RarityData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.UpgradeTable
import com.nothingmotion.brawlprogressionanalyzer.ui.components.AccordionView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.NumberFormat

@AndroidEntryPoint
class FutureProgressFragment : Fragment() {
    private var _binding: FragmentFutureProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FutureProgressViewModel by viewModels()
    private val numberFormat = NumberFormat.getIntegerInstance()

    // Add the adapter as a property of the fragment
    private val brawlerAdapter = BrawlerUnlockAdapter()
    private val brawlerUpgradeAdapter = BrawlerUpgradeAdapter()

    // Progress loading indicator
    private var isLoadingMore = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFutureProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupTimeframeSelection()
        setupPlayerTypeSelection()
        setupRecyclerView()
        setupUpgradableRecyclerView()
        setupProgressOverlay()

        // Get account ID from arguments and load account data
        arguments?.getString("accountId")?.let { accountId ->
            Timber.tag("FutureProgressFragment").d(accountId)
            viewModel.getAccount(accountId)
        }
        
        // Observe state updates
        observeViewModel()
    }

    private fun setupProgressOverlay() {
        binding.progressOverlay.apply {
            isVisible = false
            setOnClickListener { /* Consume clicks to prevent interaction */ }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe account state
                launch {
                    viewModel.state
                        .map { it.accountState }
                        .distinctUntilChanged()
                        .collectLatest { accountState ->
                            accountState?.let { updateAccountUI(it) }
                        }
                }
                
                // Observe tables state
                launch {
                    viewModel.state
                        .map { it.tablesState }
                        .distinctUntilChanged()
                        .collectLatest { tablesState ->
                            Timber.tag("FutureProgressFragment").d("Collecting tablesState: $tablesState")
                            tablesState?.let { updateTablesUI(it) }
                        }
                }
                
                // Observe calculated state
                launch {
                    viewModel.state
                        .map { it.calculatedState }
                        .distinctUntilChanged()
                        .collectLatest { calculatedState ->
                            calculatedState?.let { updateCalculatedUI(it) }
                        }
                }
                
                // Observe projected state
                launch {
                    viewModel.state
                        .map { it.projectedState }
                        .distinctUntilChanged()
                        .collectLatest { projectedState ->
                            projectedState?.let { updateProjectedUI(it) }
                        }
                }
                
                // Observe brawlers state
                launch {
                    viewModel.state
                        .map { it.brawlersState }
                        .distinctUntilChanged()
                        .collectLatest { brawlersState ->
                            brawlersState?.let { updateBrawlersUI(it) }
                        }
                }
                
                // Observe next steps advice
                launch {
                    viewModel.state
                        .map { it.nextStepsAdvice }
                        .distinctUntilChanged()
                        .collectLatest { nextStepsAdvice ->
                            nextStepsAdvice?.let { updateAdviceUI(it) }
                        }
                }
            }
        }
    }

    private fun updateAccountUI(accountState: AccountState) {
        // Only update UI elements related to account data
        accountState.account?.let { account ->
            // Show account info
            binding.progressOverlay.isVisible = false
        } ?: run {
            // Show loading or error state for account
            binding.progressOverlay.isVisible = true
        }
    }

    private fun updateTablesUI(tablesState: TablesState) {
        // Update UI components related to tables data
        tablesState.upgradeTable?.let { upgradeTable ->
            if (!upgradeTable.levels.isNullOrEmpty()) {
                populateUpgradeTable(upgradeTable)
            }
        }
        
        tablesState.brawlerTable.let { brawlerTable ->
            if (brawlerTable.isNotEmpty()) {
                populateBrawlerTable(brawlerTable)
            }
        }
        
        tablesState.starrDropRewards.let { starrDropRewards ->
            if (starrDropRewards.isNotEmpty()) {
                setupStarrDropRewards(starrDropRewards)
            }
        }
    }

    private fun updateCalculatedUI(calculatedState: CalculatedState) {
        with(binding) {
            // Total Power Points needed
            powerpointsValue.text = numberFormat.format(calculatedState.neededPowerPoints)

            // Total Coins needed
            coinsValue.text = numberFormat.format(calculatedState.neededCoins)

            // Currently maxed out brawlers
            currentlyMaxedOut.text = "(${calculatedState.maxedBrawlers}/${calculatedState.totalBrawlers})"
        }
    }

    private fun updateProjectedUI(projectedState: ProjectedState) {
        with(binding) {
            // Update future resources based on current state
            powerpointsPerMonthValue.text = numberFormat.format(projectedState.projectedPowerPoints)
            coinsPerMonthValue.text = numberFormat.format(projectedState.projectedCoins)
            creditsPerMonthValue.text = numberFormat.format(projectedState.projectedCredits)
            
            // Update months to max text
            powerpointsMonthsToMax.text = "It will take approximately ${projectedState.monthsToMaxPowerPoints} months to collect enough Power Points"
            coinsMonthsToMax.text = "It will take approximately ${projectedState.monthsToMaxCoins} months to collect enough Coins"
            
            // Update starr drops info
            totalDropsText.text = "Total: ${projectedState.totalDrops} (${projectedState.passDropsCount} Pass Drops)"
        }
    }

    private fun updateBrawlersUI(brawlersState: BrawlersState) {
        // Only show if we have data
        val projectedCredits = viewModel.state.value.projectedState?.projectedCredits ?: 0
        
        // Update unlockable brawlers section
        if (brawlersState.unlockableBrawlers.isNotEmpty() || brawlersState.lockedBrawlers.isNotEmpty()) {
            populateUnlockableBrawlers(
                brawlersState.unlockableBrawlers, 
                projectedCredits, 
                brawlersState.totalUnlockableBrawlers
            )
            
            // Show/hide load more button and progress based on state
            binding.loadMoreProgress.isVisible = isLoadingMore
            binding.loadMoreButton.isVisible = brawlersState.hasMoreBrawlers && !isLoadingMore
        }
        
        // Update upgradable brawlers section
        if (brawlersState.upgradableBrawlers.isNotEmpty()) {
            populateUpgradableBrawlers(brawlersState.upgradableBrawlers)
        } else {
            binding.upgradableBrawlersContainer.visibility = View.GONE
        }
    }

    private fun updateAdviceUI(nextStepsAdvice: NextStepsAdvice) {
        // Update the personalized advice section
        if (nextStepsAdvice.nextStepsAdvice.isNotEmpty()) {
            binding.whatsNextAdvice.text = nextStepsAdvice.nextStepsAdvice
        } else {
            // Show example advice if real advice is not available yet
            binding.whatsNextAdvice.text = viewModel.getExampleAdvice()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTimeframeSelection() {
        binding.timeframeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds[0]
                val months = when (chipId) {
                    R.id.month_1_chip -> 1
                    R.id.month_3_chip -> 3
                    R.id.month_6_chip -> 6
                    R.id.month_12_chip -> 12
                    else -> 1
                }
                viewModel.setTimeframe(months)
                viewModel.state.value.tablesState?.starrDropRewards?.let { setupStarrDropRewards(it) }
            }
        }
    }

    private fun setupPlayerTypeSelection() {
        binding.playerTypeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds[0]
                val isP2W = chipId == R.id.competitive_chip
                viewModel.setPlayerType(isP2W)
            }
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.brawlersRecyclerView
        
        // Set fixed size for performance if content doesn't change the RecyclerView size
        recyclerView.setHasFixedSize(true)
        
        // Enable scrolling for the RecyclerView
        recyclerView.isNestedScrollingEnabled = true
        
        // Optimize animation - disable item change animations that might cause lag
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.apply {
            supportsChangeAnimations = false
            changeDuration = 0
        }
        
        // Configure layout manager
        val layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.initialPrefetchItemCount = 6 // Prefetch items for smoother scrolling
        recyclerView.layoutManager = layoutManager
        
        // Set adapter with initial empty list
        recyclerView.adapter = brawlerAdapter
        
        // Add item decoration for spacing between items
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, spacingInPixels, true))
        
        // Add scroll listener for lazy loading
        recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                // Don't trigger if already loading
                if (isLoadingMore) return
                
                val layoutManager = recyclerView.layoutManager as? GridLayoutManager ?: return
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                
                // If we're near the end of the list and have more items to load
                val brawlersState = viewModel.state.value.brawlersState
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 4
                    && firstVisibleItemPosition >= 0
                    && brawlersState?.hasMoreBrawlers == true) {
                    loadMoreBrawlers()
                }
            }
        })
        
        // Set up load more button
        binding.loadMoreButton.setOnClickListener {
            loadMoreBrawlers()
        }
    }

    private fun loadMoreBrawlers() {
        val brawlersState = viewModel.state.value.brawlersState
        if (brawlersState?.hasMoreBrawlers != true || isLoadingMore) return
        
        // Set loading state
        isLoadingMore = true
        binding.loadMoreProgress.isVisible = true
        binding.loadMoreButton.isVisible = false
        
        // Load more brawlers
        viewModel.loadMoreBrawlers()
        
        // Reset loading state after a short delay (for UX)
        view?.postDelayed({
            isLoadingMore = false
            binding.loadMoreProgress.isVisible = false
            binding.loadMoreButton.isVisible = viewModel.state.value.brawlersState?.hasMoreBrawlers == true
        }, 500)
    }

    private fun setupUpgradableRecyclerView() {
        val recyclerView = binding.upgradableBrawlersRecyclerView
        
        // Set fixed size for performance if content doesn't change the RecyclerView size
        recyclerView.setHasFixedSize(true)
        
        // Enable scrolling for the RecyclerView
        recyclerView.isNestedScrollingEnabled = true
        
        // Optimize animation - disable item change animations that might cause lag
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.apply {
            supportsChangeAnimations = false
            changeDuration = 0
        }
        
        // Configure layout manager
        val layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.initialPrefetchItemCount = 6 // Prefetch items for smoother scrolling
        recyclerView.layoutManager = layoutManager
        
        // Set adapter with initial empty list
        recyclerView.adapter = brawlerUpgradeAdapter
        
        // Add item decoration for spacing between items
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, spacingInPixels, true))
        
        // Set click listener to show upgrade details
        brawlerUpgradeAdapter.setOnBrawlerUpgradeClickListener(object :
            BrawlerUpgradeAdapter.OnBrawlerUpgradeClickListener {
            override fun onBrawlerUpgradeClick(brawler: UpgradableBrawler) {
                showUpgradeDetailsDialog(brawler)
            }
        })
    }

    private fun populateUpgradeTable(upgradeTable: UpgradeTable) {
        val tableLayout = binding.upgradeTable
        tableLayout.removeAllViews()

        // Add header row
        val headerRow = LayoutInflater.from(requireContext()).inflate(
            R.layout.item_upgrade_table_header, tableLayout, false
        ) as TableRow
        tableLayout.addView(headerRow)

        // Add data rows for each level
        for (level in upgradeTable.levels) {
            val row = LayoutInflater.from(requireContext()).inflate(
                R.layout.item_upgrade_table_row, tableLayout, false
            ) as TableRow

            // Alternate row colors for better readability
            if (level.level % 2 == 0) {
                row.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.table_row_even
                    )
                )
            }

            // Set values to TextViews in the row
            row.findViewById<TextView>(R.id.level_text).text = level.level.toString()
            row.findViewById<TextView>(R.id.powerpoints_text).text = numberFormat.format(level.powerPoints)
            row.findViewById<TextView>(R.id.coins_text).text = numberFormat.format(level.coins)
            row.findViewById<TextView>(R.id.total_powerpoints_text).text = numberFormat.format(level.totalPowerPoints)
            row.findViewById<TextView>(R.id.total_coins_text).text = numberFormat.format(level.totalCoins)

            tableLayout.addView(row)
        }
    }

    private fun populateBrawlerTable(brawlerTable: List<com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable>) {
        val tableLayout = binding.creditsTable
        tableLayout.removeAllViews()

        val headerRow = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_brawler_table_header, tableLayout, false) as TableRow

        tableLayout.addView(headerRow)
        var i = 0
        for (table in brawlerTable) {
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_brawler_table_row, tableLayout, false) as TableRow
            i++
            if (i % 2 == 0) {
                row.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.table_row_even
                    )
                )
            }

            row.findViewById<TextView>(R.id.rarity_text).text = table.rarity.name
            row.findViewById<TextView>(R.id.credits_text).text = table.value.toString()

            tableLayout.addView(row)
        }
    }

    private fun setupStarrDropRewards(starrDropTable: List<StarrDropRewards>) {
        // Find the parent container where all accordions will be added
        val parentContainer = binding.starrdropsContainer
        parentContainer.removeAllViews()

        // Get the current timeframe from state
        val selectedTimeframeMonths = viewModel.getTimeframe() ?: 1

        // Create and add an accordion view for each rarity type
        for (table in starrDropTable) {
            val rarity = table.rarity
            val months = selectedTimeframeMonths * 30
            val passDrops = (29 * selectedTimeframeMonths)
            val dailyDrops = (3 * months)
            val totalDrops = (dailyDrops) + (passDrops)
            val amount = (totalDrops * table.chanceToDrop).toInt()

            // Create a new accordion view for this rarity
            val accordionView = AccordionView(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                setHeaderPadding(12)
                setTitle("x$amount ${rarity.name} (${(table.chanceToDrop * 100).toInt()}% drop chance)")

                // Set background color based on rarity
                val backgroundColor = when (rarity) {
                    RarityData.COMMON -> R.color.table_row_even
                    RarityData.RARE -> R.color.rare_color
                    RarityData.SUPER_RARE -> R.color.super_rare_color
                    RarityData.EPIC -> R.color.epic_color
                    RarityData.MYTHIC -> R.color.mythic_color
                    RarityData.LEGENDARY -> R.color.legendary_color
                }

                // Try to set background color with fallback
                try {
                    val color = ContextCompat.getColor(context, backgroundColor)
                    setCardBackgroundColor(color)
                } catch (e: Exception) {
                    Timber.e("Color resource not found: $backgroundColor", e)
                }

                setAccordionCornerRadius(8f)
                setAccordionMargins(8, 4)
            }

            // Create a grid layout for this rarity
            val gridLayout = GridLayout(requireContext()).apply {
                id = View.generateViewId()
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                columnCount = 3
                setPadding(8, 8, 8, 8)
            }

            // Add resource items to the grid
            for (reward in table.rewards) {
                val card = LayoutInflater.from(requireContext()).inflate(
                    R.layout.item_starrdrop_resource,
                    gridLayout,
                    false
                ) as MaterialCardView

                val image = card.findViewById<ImageView>(R.id.resource_image)
                val name = card.findViewById<TextView>(R.id.resource_name)
                val amount = card.findViewById<TextView>(R.id.resource_amount)
                val chance = card.findViewById<TextView>(R.id.resource_chance)

                name.text = reward.resource.name
                amount.text = reward.resource.amount.toString()
                chance.text = "${(reward.chance * 100).toInt()}%"

                // Set card margins
                val layoutParams = GridLayout.LayoutParams().apply {
                    setMargins(8, 8, 8, 8)
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                card.layoutParams = layoutParams

                // Add card to grid
                gridLayout.addView(card)
            }

            // Create a container for the grid layout
            val gridContainer = LinearLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                setPadding(8, 8, 8, 8)
            }

            // Add the grid to the container
            gridContainer.addView(gridLayout)

            // Add the container to the accordion
            accordionView.addContent(gridContainer)

            // Add accordion to parent container
            parentContainer.addView(accordionView)
        }
    }

    private fun populateUnlockableBrawlers(
        unlockableBrawlers: List<UnlockableBrawler>, 
        totalCredits: Int,
        totalBrawlersCount: Int
    ) {
        // Update summary text with total count information
        binding.brawlersSummaryText.text = buildString {
            append("With ${numberFormat.format(totalCredits)} credits, you can unlock $totalBrawlersCount brawlers")
            if (unlockableBrawlers.size < totalBrawlersCount) {
                append(" (showing ${unlockableBrawlers.size})")
            }
        }

        // Create immutable copy to avoid modification issues during animation
        val brawlersList = unlockableBrawlers.toList()
        
        // Submit the new list to the adapter - use optimized submitList with callback
        viewLifecycleOwner.lifecycleScope.launch {
            brawlerAdapter.submitList(brawlersList) {
                // This callback runs when the list diffing is complete and new list is committed
                binding.brawlersRecyclerView.post {
                    // Only scroll to top on initial load, not when loading more
                    if (brawlersList.size <= 20) {
                        binding.brawlersRecyclerView.scrollToPosition(0)
                    }
                }
            }
        }
    }

    private fun populateUpgradableBrawlers(upgradableBrawlers: List<UpgradableBrawler>) {
        // Update summary text with count information
        binding.upgradableBrawlersSummaryText.text = buildString {
            append("With your resources, you can upgrade ${upgradableBrawlers.size} brawlers")
            
            // Calculate the total level increase across all brawlers
            val totalLevelIncrease = upgradableBrawlers.sumOf { it.to - it.from }
            if (totalLevelIncrease > 0) {
                append(" (total +$totalLevelIncrease power levels)")
            }
        }
        
        // Set visibility of the container based on whether we have upgradable brawlers
        binding.upgradableBrawlersContainer.visibility = if (upgradableBrawlers.isNotEmpty()) View.VISIBLE else View.GONE
        
        // Create immutable copy to avoid modification issues during animation
        val upgradesList = upgradableBrawlers.toList()
        
        // Submit the new list to the adapter with callback for smooth updates
        viewLifecycleOwner.lifecycleScope.launch {
            brawlerUpgradeAdapter.submitList(upgradesList) {
                // This callback runs when the list diffing is complete and new list is committed
                binding.upgradableBrawlersRecyclerView.post {
                    // Scroll to top when new data is loaded
                    binding.upgradableBrawlersRecyclerView.scrollToPosition(0)
                }
            }
        }
    }

    /**
     * Item decoration to add spacing between grid items
     */
    private class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: android.graphics.Rect,
            view: View,
            parent: androidx.recyclerview.widget.RecyclerView,
            state: androidx.recyclerview.widget.RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                
                if (position < spanCount) {
                    outRect.top = spacing
                }
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                
                if (position >= spanCount) {
                    outRect.top = spacing
                }
            }
        }
    }

    /**
     * Show a dialog with detailed information about the brawler upgrade
     */
    private fun showUpgradeDetailsDialog(brawler: UpgradableBrawler) {
        // Get the cost information from the upgrade table
        val upgradeTable = viewModel.state.value.tablesState?.upgradeTable ?: return
        
        // Calculate resources needed for this specific upgrade
        var totalPowerPoints = 0
        var totalCoins = 0
        
        for (level in brawler.from until brawler.to) {
            val upgradeCost = upgradeTable.levels.find { it.level == level + 1 }
            if (upgradeCost != null) {
                totalPowerPoints += upgradeCost.powerPoints
                totalCoins += upgradeCost.coins
            }
        }
        
        // Create and show a Material dialog
        val context = requireContext()
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_brawler_upgrade_details, null)
        
        // Find and set up views
        val brawlerName = dialogView.findViewById<TextView>(R.id.upgrade_brawler_name)
        val powerPointsRequired = dialogView.findViewById<TextView>(R.id.power_points_required)
        val coinsRequired = dialogView.findViewById<TextView>(R.id.coins_required)
        val levelDetails = dialogView.findViewById<TextView>(R.id.level_details)
        
        // Set values
        brawlerName.text = brawler.name
        powerPointsRequired.text = "Power Points: ${numberFormat.format(totalPowerPoints)}"
        coinsRequired.text = "Coins: ${numberFormat.format(totalCoins)}"
        levelDetails.text = "From Power ${brawler.from} to Power ${brawler.to}"
        
        // Create and show the dialog
        builder.setView(dialogView)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

