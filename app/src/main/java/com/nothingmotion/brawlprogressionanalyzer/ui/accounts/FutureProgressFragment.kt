package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.data.FakeUpgradeTableRepository
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentFutureProgressBinding
import com.nothingmotion.brawlprogressionanalyzer.model.Account
import com.nothingmotion.brawlprogressionanalyzer.model.UpgradeTable
import com.nothingmotion.brawlprogressionanalyzer.model.UpgradeTableLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import javax.inject.Inject
import kotlin.math.ceil

@AndroidEntryPoint
class FutureProgressFragment : Fragment() {
    private var _binding: FragmentFutureProgressBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AccountDetailViewModel by viewModels()
    private val numberFormat = NumberFormat.getIntegerInstance()
    
    @Inject
    lateinit var upgradeTableRepository: FakeUpgradeTableRepository
    
    private lateinit var account: Account
    private lateinit var upgradeTable: UpgradeTable
    
    // Resource estimates per month (based on game averages)
    private val basePowerPointsPerMonth = 2500
    private val baseCoinsPerMonth = 4600
    
    // Currently selected timeframe in months
    private var selectedTimeframeMonths = 1
    
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
        
        // Get account ID from arguments and load account data
        arguments?.getString("accountId")?.let { accountId ->
            observeAccount(accountId)
        }
        
        // Observe the upgrade table
        observeUpgradeTable()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupTimeframeSelection() {
        binding.timeframeChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds[0]
                selectedTimeframeMonths = when (chipId) {
                    R.id.month_1_chip -> 1
                    R.id.month_3_chip -> 3
                    R.id.month_6_chip -> 6
                    R.id.month_12_chip -> 12
                    else -> 1
                }
                
                // Update future resources based on selected timeframe
                if (::account.isInitialized) {
                    updateFutureResources()
                }
            }
        }
    }
    
    private fun observeAccount(accountId: String) {
        // Load account details
        viewModel.getAccount(accountId)
        
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.account.collectLatest { account ->
                    account?.let {
                        this@FutureProgressFragment.account = it
                        if (::upgradeTable.isInitialized) {
                            updateUI()
                        }
                    }
                }
            }
        }
    }
    
    private fun observeUpgradeTable() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                upgradeTableRepository.upgradeTable.collectLatest { upgradeTable ->
                    this@FutureProgressFragment.upgradeTable = upgradeTable
                    if (::account.isInitialized) {
                        updateUI()
                    }
                }
            }
        }
    }
    
    private fun updateUI() {
        // Calculate resources needed
        val resourcesNeeded = calculateResourcesNeeded()
        
        // Update UI with calculated values
        with(binding) {
            // Total Power Points needed
            powerpointsValue.text = numberFormat.format(resourcesNeeded.first)
            
            // Total Coins needed
            coinsValue.text = numberFormat.format(resourcesNeeded.second)
            
            // Populate upgrade table
            populateUpgradeTable()
            
            // Update future resources based on current timeframe selection
            updateFutureResources()
        }
    }
    
    /**
     * Calculate power points and coins needed to max out all brawlers
     * Returns a Pair<PowerPoints, Coins>
     */
    private fun calculateResourcesNeeded(): Pair<Int, Int> {
        // Calculate brawler counts from the brawlers list
        val brawlerCount = account.account.brawlers.size
        val maxedBrawlers = account.account.brawlers.count { it.power >= 11 }
        val nonMaxedBrawlers = brawlerCount - maxedBrawlers
        
        // Assuming a brawler is "maxed" at level 11
        // We need to calculate the total power points and coins to get from current average level to max
        val currentAveragePower = account.currentProgress.averageBrawlerPower
        
        var totalPowerPointsNeeded = 0
        var totalCoinsNeeded = 0
        
        // Calculate resources needed per non-maxed brawler
        if (currentAveragePower < 11) {
            // Find the upgrade level that matches the current average power
            val currentLevel = upgradeTable.levels.find { it.level == currentAveragePower }
                ?: upgradeTable.levels.first()
            
            // Get total resources needed from current level to max (level 11)
            val maxLevel = upgradeTable.levels.last()
            
            // Total resources for fully maxing = max level totals - current level totals
            totalPowerPointsNeeded = (maxLevel.totalPowerPoints - currentLevel.totalPowerPoints) * nonMaxedBrawlers
            totalCoinsNeeded = (maxLevel.totalCoins - currentLevel.totalCoins) * nonMaxedBrawlers
        }
        
        return Pair(totalPowerPointsNeeded, totalCoinsNeeded)
    }
    
    private fun populateUpgradeTable() {
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
                row.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.table_row_even))
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
    
    private fun updateFutureResources() {
        val resourcesNeeded = calculateResourcesNeeded()
        val powerPointsNeeded = resourcesNeeded.first
        val coinsNeeded = resourcesNeeded.second
        
        // Calculate projected resources based on timeframe
        val powerPointsPerMonth = basePowerPointsPerMonth
        val coinsPerMonth = baseCoinsPerMonth
        
        val totalPowerPointsInTimeframe = powerPointsPerMonth * selectedTimeframeMonths
        val totalCoinsInTimeframe = coinsPerMonth * selectedTimeframeMonths
        
        // Update UI with calculated values
        with(binding) {
            // Power points per month
            powerpointsPerMonthValue.text = numberFormat.format(powerPointsPerMonth)
            
            // Coins per month
            coinsPerMonthValue.text = numberFormat.format(coinsPerMonth)
            
            // Months to max out calculations
            val monthsToMaxPowerPoints = ceil(powerPointsNeeded.toDouble() / powerPointsPerMonth).toInt()
            val monthsToMaxCoins = ceil(coinsNeeded.toDouble() / coinsPerMonth).toInt()
            
            // Show estimated time to max out
            powerpointsMonthsToMax.text = "It will take approximately $monthsToMaxPowerPoints months to collect enough Power Points"
            coinsMonthsToMax.text = "It will take approximately $monthsToMaxCoins months to collect enough Coins"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 