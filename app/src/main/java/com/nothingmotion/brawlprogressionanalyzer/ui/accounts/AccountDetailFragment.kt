package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentAccountDetailBinding
import com.nothingmotion.brawlprogressionanalyzer.databinding.DialogEditTagBinding
import com.nothingmotion.brawlprogressionanalyzer.model.Account
import com.nothingmotion.brawlprogressionanalyzer.model.Player
import com.nothingmotion.brawlprogressionanalyzer.model.Progress
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@AndroidEntryPoint
class AccountDetailFragment : Fragment() {
    private var _binding: FragmentAccountDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AccountDetailViewModel by viewModels()
    private val numberFormat = DecimalFormat("#,###")
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    private lateinit var account : Account
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupAppBarScrollBehavior()
        
        // Get accountId from arguments
        arguments?.getString("accountId")?.let { accountId ->
            observeAccount(accountId)
        }
        
        setupEditButton()
        setupFutureProgressButton()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_account_detail_to_accounts)
        }
        
        // Set CollapsedTitleTextAppearance to control text visibility
        binding.collapsingToolbar.setCollapsedTitleTextAppearance(R.style.TextAppearance_App_CollapsingToolbar_Collapsed)
        binding.collapsingToolbar.setExpandedTitleTextAppearance(R.style.TextAppearance_App_CollapsingToolbar_Expanded)
        
        // Set up share button
        binding.shareButton.setOnClickListener {
            shareAccountInfo()
        }
        
        // Apply custom padding to toolbar
        adjustTitleAppearance()
    }
    
    private fun adjustTitleAppearance() {
        // Just apply additional padding directly to the toolbar
        binding.toolbar.setPadding(
            binding.toolbar.paddingLeft,
            binding.toolbar.paddingTop,
            binding.toolbar.paddingRight,
            resources.getDimensionPixelSize(R.dimen.toolbar_title_bottom_padding)
        )
        
        // Set extra bottom margin on the collapsing toolbar title
        binding.collapsingToolbar.setExpandedTitleMargin(
            binding.collapsingToolbar.expandedTitleMarginStart,
            binding.collapsingToolbar.expandedTitleMarginTop,
            binding.collapsingToolbar.expandedTitleMarginEnd,
            resources.getDimensionPixelSize(R.dimen.toolbar_bottom_margin)
        )
    }
    
    private fun setupAppBarScrollBehavior() {
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val scrollRange = appBarLayout.totalScrollRange
            val percentage = abs(verticalOffset).toFloat() / scrollRange.toFloat()
            
            // When less than 50% collapsed, hide the title
            if (percentage < 0.5) {
                if (binding.collapsingToolbar.title != null) {
                    binding.collapsingToolbar.title = null
                }
            } 
            // Otherwise show the title
            else {
                if (binding.collapsingToolbar.title == null) {
                    if (::account.isInitialized) {
                        binding.collapsingToolbar.title = account.account.name
                    }
                }
            }
        })
    }
    
    private fun observeAccount(accountId: String) {
        // Load account details
        viewModel.getAccount(accountId)
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.account.collectLatest { account ->
                    account?.let {
                        this@AccountDetailFragment.account = it
                        updateUI(it)
                    }
                }
            }
        }
    }
    
    private fun updateUI(account: Account) {
        with(binding) {
            // Header information
            accountName.text = account.account.name
            accountTag.text = account.account.tag
            accountLevel.text = getString(R.string.level_format, account.account.level)
            
            // Stats card
            accountTrophies.text = numberFormat.format(account.account.trophies)
            accountHighestTrophies.text = numberFormat.format(account.account.highestTrophies)
            
            // Calculate brawler counts from the brawlers list
            val brawlerCount = account.account.brawlers.size
            accountBrawlers.text = brawlerCount.toString()
            
            // Count maxed brawlers (power level 11)
            val maxedBrawlers = account.account.brawlers.count { it.power >= 11 }
            accountMaxedBrawlers.text = maxedBrawlers.toString()
            
            // Check if all brawlers are maxed and show the how to max out text if not
            if (maxedBrawlers < brawlerCount) {
                howToMaxOut.visibility = View.VISIBLE
                // Show text as clickable
                howToMaxOut.setOnClickListener {
                    navigateToFutureProgress()
                }
            } else {
                howToMaxOut.visibility = View.GONE
            }
            
            // Progress
            val progressPercentage = if (brawlerCount > 0) {
                (maxedBrawlers.toFloat() / brawlerCount.toFloat()) * 100
            } else {
                0f
            }
            
            maxedProgress.progress = progressPercentage.toInt()
            progressionPercentage.text = getString(
                R.string.maxed_percentage_format,
                progressPercentage.toInt()
            )
            
            // Last updated
            lastUpdated.text = getString(
                R.string.last_updated_format,
                dateFormat.format(account.updatedAt)
            )
            
            // Progression history charts
            setupProgressionCharts(account)
            setupProgressSummary(account)
            // Set toolbar title only when collapsed, handled by AppBarLayout.OnOffsetChangedListener
        }
    }
    
    private fun setupProgressionCharts(account: Account) {
        // Check if we have history data
        val historyData = account.history
        val progressHistory = account.previousProgresses.orEmpty()
        
        if (historyData.isNullOrEmpty() && progressHistory.isEmpty()) {
            // If no history, show empty state
            binding.noHistoryText.visibility = View.VISIBLE
            binding.chartsContainer.visibility = View.GONE
            binding.trackProgressionButton.visibility = View.VISIBLE
        } else {
            // We have history, setup charts
            binding.noHistoryText.visibility = View.GONE
            binding.chartsContainer.visibility = View.VISIBLE
            
            // Setup all charts
            if (!historyData.isNullOrEmpty()) {
                binding.trophyChartContainer.visibility = View.VISIBLE
                setupTrophyChart(account)
                
                binding.brawlerChartContainer.visibility = View.VISIBLE
                setupBrawlerCountChart(account)
            } else {
                binding.trophyChartContainer.visibility = View.GONE
                binding.brawlerChartContainer.visibility = View.GONE
            }
            
            if (progressHistory.isNotEmpty()) {
                binding.creditsChartContainer.visibility = View.VISIBLE
                binding.coinsChartContainer.visibility = View.VISIBLE
                binding.powerPointsChartContainer.visibility = View.VISIBLE
                
                setupCreditsChart(account)
                setupCoinsChart(account)
                setupPowerPointsChart(account)
            } else {
                binding.creditsChartContainer.visibility = View.GONE
                binding.coinsChartContainer.visibility = View.GONE
                binding.powerPointsChartContainer.visibility = View.GONE
            }
            
            // Hide the track button since we already have data
            binding.trackProgressionButton.visibility = View.GONE
        }
        
        // Setup track progression button
        binding.trackProgressionButton.setOnClickListener {
            Toast.makeText(context, "Tracking progression...", Toast.LENGTH_SHORT).show()
            // TODO: Implement tracking progression functionality
        }
    }
    
    // 1. Trophy History Chart
    private fun setupTrophyChart(account: Account) {
        val chart = binding.trophyHistoryChart
        
        // Clear any existing data
        chart.clear()
        
        // Get history data + current state for trophy history
        val historyData = account.history.orEmpty().sortedBy { it.createdAt.time }
        
        // Create entries for trophy history
        val entries = mutableListOf<Entry>()
        val xLabels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())
        
        // Add historical data points
        historyData.forEachIndexed { index, player ->
            entries.add(Entry(index.toFloat(), player.trophies.toFloat()))
            xLabels.add(dateFormat.format(Date(player.createdAt.time)))
        }
        
        // Add current data point
        entries.add(Entry(historyData.size.toFloat(), account.account.trophies.toFloat()))
        xLabels.add(dateFormat.format(account.updatedAt))
        
        // Find min and max values for better Y axis scaling
        val minTrophies = entries.minByOrNull { it.y }?.y ?: 0f
        val maxTrophies = entries.maxByOrNull { it.y }?.y ?: account.account.trophies.toFloat()
        val yAxisRange = maxTrophies - minTrophies
        val yAxisMin = maxOf(0f, minTrophies - (yAxisRange * 0.1f)) // 10% padding below
        val yAxisMax = maxTrophies + (yAxisRange * 0.1f) // 10% padding above
        
        // Create dataset
        val dataSet = LineDataSet(entries, getString(R.string.trophy_history)).apply {
            color = ContextCompat.getColor(requireContext(), R.color.chart_line)
            lineWidth = 2.5f
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.chart_point))
            circleRadius = 4f
            circleHoleRadius = 2f
            circleHoleColor = Color.WHITE
            
            // Values above data points
            setDrawValues(true)
            valueTextSize = 9f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.chart_label)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return NumberFormat.getIntegerInstance().format(value.toLong())
                }
            }
            
            // Smooth curve and fill
            mode = LineDataSet.Mode.CUBIC_BEZIER
            fillColor = ContextCompat.getColor(requireContext(), R.color.chart_line_fill)
            fillAlpha = 85
            setDrawFilled(true)
            
            // Highlight
            highLightColor = ContextCompat.getColor(requireContext(), R.color.chart_point)
            highlightLineWidth = 1.5f
        }
        
        // Create and set line data
        val lineData = LineData(dataSet)
        chart.data = lineData
        
        // Customize chart appearance
        setupLineChart(chart, xLabels, yAxisMin, yAxisMax)
    }
    
    // 2. Brawlers Amount Chart
    private fun setupBrawlerCountChart(account: Account) {
        val chart = binding.brawlerCountChart
        
        // Clear any existing data
        chart.clear()
        
        // Get history + current data
        val historyData = account.history.orEmpty().sortedBy { it.createdAt.time }
        
        // Create entries for brawler count history
        val entries = mutableListOf<Entry>()
        val xLabels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())
        
        // Add historical data points
        historyData.forEachIndexed { index, player ->
            entries.add(Entry(index.toFloat(), player.brawlers.size.toFloat()))
            xLabels.add(dateFormat.format(Date(player.createdAt.time)))
        }
        
        // Add current data point
        entries.add(Entry(historyData.size.toFloat(), account.account.brawlers.size.toFloat()))
        xLabels.add(dateFormat.format(account.updatedAt))
        
        // Find min and max values for better Y axis scaling
        val maxBrawlers = entries.maxByOrNull { it.y }?.y ?: account.account.brawlers.size.toFloat()
        val minBrawlers = entries.minByOrNull { it.y }?.y ?: 0f
        val yAxisRange = maxBrawlers - minBrawlers
        val yAxisMin = maxOf(0f, minBrawlers - (yAxisRange * 0.1f)) // 10% padding below
        val yAxisMax = maxBrawlers + (yAxisRange * 0.1f) // 10% padding above
        
        // Create dataset
        val dataSet = LineDataSet(entries, getString(R.string.brawler_count)).apply {
            color = ContextCompat.getColor(requireContext(), R.color.chart_bar_1)
            lineWidth = 2.5f
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.chart_bar_1))
            circleRadius = 4f
            circleHoleRadius = 2f
            circleHoleColor = Color.WHITE
            
            // Values above data points
            setDrawValues(true)
            valueTextSize = 9f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.chart_label)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
            
            // Smooth curve and fill
            mode = LineDataSet.Mode.CUBIC_BEZIER
            fillColor = ContextCompat.getColor(requireContext(), R.color.chart_bar_1)
            fillAlpha = 50
            setDrawFilled(true)
            
            // Highlight
            highLightColor = ContextCompat.getColor(requireContext(), R.color.chart_point)
            highlightLineWidth = 1.5f
        }
        
        // Create and set line data
        val lineData = LineData(dataSet)
        chart.data = lineData
        
        // Customize chart appearance
        setupLineChart(chart, xLabels, yAxisMin, yAxisMax)
    }
    
    // 3. Credits Gained Chart
    private fun setupCreditsChart(account: Account) {
        val chart = binding.creditsChart
        
        // Clear any existing data
        chart.clear()
        
        // Get progress history data
        val progressHistory = account.previousProgresses.orEmpty().sortedBy { it.duration.time }
        if (progressHistory.isEmpty()) {
            binding.creditsChartContainer.visibility = View.GONE
            return
        }
        
        // Create entries for credits history
        val entries = mutableListOf<BarEntry>()
        val xLabels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())
        
        // Add data points
        progressHistory.forEachIndexed { index, progress ->
            entries.add(BarEntry(index.toFloat(), progress.credits.toFloat()))
            xLabels.add(dateFormat.format(progress.duration))
        }
        
        // Find max value for better Y axis scaling
        val maxCredits = entries.maxByOrNull { it.y }?.y ?: 0f
        val yAxisMax = maxCredits * 1.15f // 15% padding
        
        // Create dataset
        val dataSet = BarDataSet(entries, getString(R.string.credits_gained)).apply {
            color = ContextCompat.getColor(requireContext(), R.color.chart_bar_2)
            valueTextSize = 9f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.chart_label)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
            // Highlight color
            highLightColor = ContextCompat.getColor(requireContext(), R.color.chart_point)
        }
        
        // Create bar data
        val barData = BarData(dataSet)
        barData.barWidth = 0.7f
        
        chart.data = barData
        
        // Customize chart appearance
        setupBarChart(chart, xLabels, 0f, yAxisMax)
    }
    
    // 4. Coins Gained Chart
    private fun setupCoinsChart(account: Account) {
        val chart = binding.coinsChart
        
        // Clear any existing data
        chart.clear()
        
        // Get progress history data
        val progressHistory = account.previousProgresses.orEmpty().sortedBy { it.duration.time }
        if (progressHistory.isEmpty()) {
            binding.coinsChartContainer.visibility = View.GONE
            return
        }
        
        // Create entries for coins history
        val entries = mutableListOf<BarEntry>()
        val xLabels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())
        
        // Add data points
        progressHistory.forEachIndexed { index, progress ->
            entries.add(BarEntry(index.toFloat(), progress.coins.toFloat()))
            xLabels.add(dateFormat.format(progress.duration))
        }
        
        // Find max value for better Y axis scaling
        val maxCoins = entries.maxByOrNull { it.y }?.y ?: 0f
        val yAxisMax = maxCoins * 1.15f // 15% padding
        
        // Create dataset
        val dataSet = BarDataSet(entries, getString(R.string.coins_gained)).apply {
            color = ContextCompat.getColor(requireContext(), R.color.chart_bar_1)
            valueTextSize = 9f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.chart_label)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return NumberFormat.getIntegerInstance().format(value.toLong())
                }
            }
            // Highlight color
            highLightColor = ContextCompat.getColor(requireContext(), R.color.chart_point)
        }
        
        // Create bar data
        val barData = BarData(dataSet)
        barData.barWidth = 0.7f
        
        chart.data = barData
        
        // Customize chart appearance
        setupBarChart(chart, xLabels, 0f, yAxisMax)
    }
    
    // 5. Power Points Gained Chart
    private fun setupPowerPointsChart(account: Account) {
        val chart = binding.powerPointsChart
        
        // Clear any existing data
        chart.clear()
        
        // Get progress history data
        val progressHistory = account.previousProgresses.orEmpty().sortedBy { it.duration.time }
        if (progressHistory.isEmpty()) {
            binding.powerPointsChartContainer.visibility = View.GONE
            return
        }
        
        // Create entries for power points history
        val entries = mutableListOf<BarEntry>()
        val xLabels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())
        
        // Add data points
        progressHistory.forEachIndexed { index, progress ->
            entries.add(BarEntry(index.toFloat(), progress.powerPoints.toFloat()))
            xLabels.add(dateFormat.format(progress.duration))
        }
        
        // Find max value for better Y axis scaling
        val maxPowerPoints = entries.maxByOrNull { it.y }?.y ?: 0f
        val yAxisMax = maxPowerPoints * 1.15f // 15% padding
        
        // Create dataset
        val dataSet = BarDataSet(entries, getString(R.string.power_points_gained)).apply {
            color = ContextCompat.getColor(requireContext(), R.color.purple_500)
            valueTextSize = 9f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.chart_label)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return NumberFormat.getIntegerInstance().format(value.toLong())
                }
            }
            // Highlight color
            highLightColor = ContextCompat.getColor(requireContext(), R.color.chart_point)
        }
        
        // Create bar data
        val barData = BarData(dataSet)
        barData.barWidth = 0.7f
        
        chart.data = barData
        
        // Customize chart appearance
        setupBarChart(chart, xLabels, 0f, yAxisMax)
    }
    
    // Helper method to set up common line chart properties
    private fun setupLineChart(chart: LineChart, xLabels: List<String>, yAxisMin: Float, yAxisMax: Float) {
        chart.apply {
            description.isEnabled = false
            
            // X-axis setup
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
            xAxis.labelRotationAngle = -25f
            xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.chart_label)
            xAxis.textSize = 10f
            xAxis.setDrawAxisLine(true)
            xAxis.axisLineWidth = 1.5f
            xAxis.axisLineColor = ContextCompat.getColor(requireContext(), R.color.chart_grid)
            xAxis.yOffset = 10f
            
            // Left Y-axis setup
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = ContextCompat.getColor(requireContext(), R.color.chart_grid)
            axisLeft.gridLineWidth = 0.7f
            axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.chart_label)
            axisLeft.textSize = 10f
            axisLeft.axisMinimum = yAxisMin
            axisLeft.axisMaximum = yAxisMax
            axisLeft.setDrawZeroLine(true)
            axisLeft.zeroLineWidth = 1.5f
            axisLeft.zeroLineColor = ContextCompat.getColor(requireContext(), R.color.chart_grid)
            axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return NumberFormat.getIntegerInstance().format(value.toLong())
                }
            }
            
            // Right Y-axis setup
            axisRight.isEnabled = false
            
            // Legend setup
            legend.isEnabled = false
            
            // Interactions
            isDoubleTapToZoomEnabled = true
            isDragEnabled = true
            setScaleEnabled(true)
            setVisibleXRangeMaximum(6f) // Show max 6 data points at a time for better visibility
            
            // Extra space
            extraBottomOffset = 10f
            
            // Animation
            animateY(700)
        }
        
        // Refresh chart
        chart.invalidate()
    }
    
    // Helper method to set up common bar chart properties
    private fun setupBarChart(chart: BarChart, xLabels: List<String>, yAxisMin: Float, yAxisMax: Float) {
        chart.apply {
            description.isEnabled = false
            
            // X-axis setup
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
            xAxis.labelRotationAngle = -25f
            xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.chart_label)
            xAxis.textSize = 10f
            xAxis.setDrawAxisLine(true)
            xAxis.axisLineWidth = 1.5f
            xAxis.axisLineColor = ContextCompat.getColor(requireContext(), R.color.chart_grid)
            xAxis.yOffset = 10f
            
            // Left Y-axis setup
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = ContextCompat.getColor(requireContext(), R.color.chart_grid)
            axisLeft.gridLineWidth = 0.7f
            axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.chart_label)
            axisLeft.textSize = 10f
            axisLeft.axisMinimum = yAxisMin
            axisLeft.axisMaximum = yAxisMax
            axisLeft.setDrawZeroLine(true)
            axisLeft.zeroLineWidth = 1.5f
            axisLeft.zeroLineColor = ContextCompat.getColor(requireContext(), R.color.chart_grid)
            
            // Right Y-axis setup
            axisRight.isEnabled = false
            
            // Legend setup
            legend.isEnabled = false
            
            // Interactions
            isDoubleTapToZoomEnabled = true
            isDragEnabled = true
            setScaleEnabled(true)
            setVisibleXRangeMaximum(6f) // Show max 6 data points at a time for better visibility
            
            // Extra space
            extraBottomOffset = 10f
            
            // Animation
            animateY(700)
        }
        
        // Refresh chart
        chart.invalidate()
    }
    
    private fun setupEditButton() {
        binding.fabEditAccount.setOnClickListener {
            // Get the current account ID from the ViewModel
            viewModel.account.value?.let { account ->
                // Show refreshing message
                Toast.makeText(context, "Refreshing account...", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to edit account screen
                // In the future, pass the account ID to an edit screen
                // For now, just show a placeholder message
            }
        }
    }
    
    private fun shareAccountInfo() {
        val shareText = buildString {
            append("Brawl Stars Player Info:\n\n")
            append("Name: ${account.account.name}\n")
            append("Tag: ${account.account.tag}\n")
            append("Level: ${account.account.level}\n")
            append("Trophies: ${numberFormat.format(account.account.trophies)}\n")
            append("Highest Trophies: ${numberFormat.format(account.account.highestTrophies)}\n")
            
            // Calculate brawlers and maxed brawlers counts
            val brawlerCount = account.account.brawlers.size
            val maxedBrawlers = account.account.brawlers.count { it.power >= 11 }
            append("Brawlers: $brawlerCount ($maxedBrawlers maxed)\n")
            append("\nShared from Brawl Progression Analyzer")
        }
        
        // Show share dialog with copy and share options
        showShareDialog(shareText)
    }
    
    private fun showShareDialog(shareText: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_share_account, null)
        
        // Set account details in the dialog
        val accountDetails = dialogView.findViewById<TextView>(R.id.account_details)
        accountDetails.text = shareText
        
        // Setup copy button
        val copyButton = dialogView.findViewById<MaterialButton>(R.id.copy_button)
        copyButton.setOnClickListener {
            copyToClipboard(shareText)
        }
        
        // Setup share button
        val shareButton = dialogView.findViewById<MaterialButton>(R.id.share_button)
        shareButton.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_account)))
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
    
    private fun copyToClipboard(text: String) {
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Account Info", text)
        clipboardManager.setPrimaryClip(clipData)
        
        // Show confirmation
        Toast.makeText(context, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }
    
    private fun setupUI(account: Account) {
        // Bind account data
        with(binding) {
            collapsingToolbar.title = account.account.name
            accountName.text = account.account.name
            accountTag.text = account.account.tag
            accountLevel.text = getString(R.string.level_format, account.account.level)
            
            // Account stats
            accountTrophies.text = NumberFormat.getIntegerInstance().format(account.account.trophies)
            accountHighestTrophies.text = NumberFormat.getIntegerInstance().format(account.account.highestTrophies)
            
            // Calculate brawler counts from the brawlers list
            val brawlerCount = account.account.brawlers.size
            val maxedBrawlers = account.account.brawlers.count { it.power >= 11 }
            
            accountBrawlers.text = brawlerCount.toString()
            accountMaxedBrawlers.text = maxedBrawlers.toString()
            
            // Calculate max percentage
            val maxPercentage = if (brawlerCount > 0) {
                (maxedBrawlers * 100) / brawlerCount
            } else {
                0
            }
            
            maxedProgress.progress = maxPercentage
            progressionPercentage.text = getString(R.string.maxed_percentage_format, maxPercentage)
            
            // Setup progress summary
            setupProgressSummary(account)
            
            // Setup progression charts
            setupProgressionCharts(account)
            
            // Show last update time
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            lastUpdated.text = getString(R.string.last_updated_format, 
                dateFormat.format(account.updatedAt))
                
            // Callback for avatar loading
            // TODO: Replace with actual avatar loading
        }
    }
    
    private fun setupProgressSummary(account: Account) {
        // Check if we have history data for calculating progress
        val historyData = account.history.orEmpty()
        val progressHistory = account.previousProgresses.orEmpty()
        
        if (historyData.isEmpty() && progressHistory.isEmpty()) {
            // Hide the progress summary if no history data available
            binding.progressHistoryCard.visibility = View.GONE
            return
        }
        
        binding.progressHistoryCard.visibility = View.VISIBLE
        
        // Calculate trophy progression
        if (historyData.isNotEmpty()) {
            val oldestPlayer = historyData.minByOrNull { it.createdAt.time }
            
            // Calculate accumulative trophy gains
            // Sort history data by timestamp to process in chronological order
            val sortedHistoryData = historyData.sortedBy { it.createdAt.time }
            
            // Start with the oldest record
            var previousTrophies = sortedHistoryData.first().trophies
            var totalTrophiesGained = 0
            
            // Iterate through each record, calculating positive gains only
            for (i in 1 until sortedHistoryData.size) {
                val currentTrophies = sortedHistoryData[i].trophies
                val difference = currentTrophies - previousTrophies
                
                // Only add positive gains to the total (ignore trophy losses)
                if (difference > 0) {
                    totalTrophiesGained += difference
                }
                
                previousTrophies = currentTrophies
            }
            
            // Also check if there's a gain from the last history record to the current account
            val lastHistoryTrophies = sortedHistoryData.last().trophies
            val finalDifference = account.account.trophies - lastHistoryTrophies
            if (finalDifference > 0) {
                totalTrophiesGained += finalDifference
            }
            
            // Calculate net gain (can be negative if player lost trophies)
            val netTrophiesGained = account.account.trophies - sortedHistoryData.first().trophies
            
            // Calculate percentage based on the starting value
            val trophiesPercentage = if (oldestPlayer != null && oldestPlayer.trophies > 0) {
                (netTrophiesGained * 100.0 / oldestPlayer.trophies).toFloat()
            } else 0f
            
            // Set trophy values (always show accumulative gains, which is always positive or zero)
            binding.trophiesGainedValue.text = "+${NumberFormat.getIntegerInstance().format(totalTrophiesGained)}"
            
            // For percentage, show the net percentage change
            binding.trophiesGainedPercentage.text = if (trophiesPercentage > 0) 
                "(+${String.format("%.1f", trophiesPercentage)}%)" 
            else 
                "(${String.format("%.1f", trophiesPercentage)}%)"
            
            // Color based on net gain/loss
            binding.trophiesGainedPercentage.setTextColor(
                ContextCompat.getColor(requireContext(), 
                    if (netTrophiesGained >= 0) R.color.chart_line else R.color.delete_background)
            )
            
            // Calculate brawler progression
            val oldBrawlerCount = oldestPlayer?.brawlers?.size ?: 0
            val newBrawlerCount = account.account.brawlers.size
            val brawlersGained = newBrawlerCount - oldBrawlerCount
            
            val brawlersPercentage = if (oldBrawlerCount > 0) {
                (brawlersGained * 100.0 / oldBrawlerCount).toFloat()
            } else 0f
            
            // Set brawler values
            binding.brawlersGainedValue.text = if (brawlersGained > 0) "+$brawlersGained" else "$brawlersGained"
            binding.brawlersGainedPercentage.text = if (brawlersPercentage > 0) 
                "(+${String.format("%.1f", brawlersPercentage)}%)" 
            else 
                "(${String.format("%.1f", brawlersPercentage)}%)"
            binding.brawlersGainedPercentage.setTextColor(
                ContextCompat.getColor(requireContext(), 
                    if (brawlersGained >= 0) R.color.chart_line else R.color.delete_background)
            )
        } else {
            // Hide trophy and brawlers sections if no player history
            binding.trophiesGainedLabel.visibility = View.GONE
            binding.trophiesGainedValue.visibility = View.GONE
            binding.trophiesGainedPercentage.visibility = View.GONE
            
            binding.brawlersGainedLabel.visibility = View.GONE
            binding.brawlersGainedValue.visibility = View.GONE
            binding.brawlersGainedPercentage.visibility = View.GONE
        }
        
        // Calculate progression from Progress history
        if (progressHistory.isNotEmpty()) {
            val totalProgress = progressHistory.fold(account.currentProgress) { acc, progress ->
                Progress(
                    coins = acc.coins + progress.coins,
                    powerPoints = acc.powerPoints + progress.powerPoints,
                    credits = acc.credits + progress.credits,
                    gears = acc.gears + progress.gears,
                    starPowers = acc.starPowers + progress.starPowers,
                    gadgets = acc.gadgets + progress.gadgets,
                    brawlers = acc.brawlers, // Not cumulative
                    averageBrawlerPower = acc.averageBrawlerPower, // Average is not cumulative
                    averageBrawlerTrophies = acc.averageBrawlerTrophies, // Average is not cumulative
                    isBoughtPass = acc.isBoughtPass,
                    isBoughtPassPlus = acc.isBoughtPassPlus,
                    isBoughRankedPass = acc.isBoughRankedPass,
                    duration = acc.duration
                )
            }
            
            // Credits
            val creditsGained = totalProgress.credits - account.currentProgress.credits
            val creditsPercentage = if (account.currentProgress.credits > 0) {
                (creditsGained * 100.0 / account.currentProgress.credits).toFloat()
            } else 100f
            
            binding.creditsGainedValue.text = if (creditsGained > 0) "+$creditsGained" else "$creditsGained"
            binding.creditsGainedPercentage.text = if (creditsPercentage > 0) 
                "(+${String.format("%.1f", creditsPercentage)}%)" 
            else 
                "(${String.format("%.1f", creditsPercentage)}%)"
            binding.creditsGainedPercentage.setTextColor(
                ContextCompat.getColor(requireContext(), 
                    if (creditsGained >= 0) R.color.chart_line else R.color.delete_background)
            )
            
            // Coins
            val coinsGained = totalProgress.coins - account.currentProgress.coins
            val coinsPercentage = if (account.currentProgress.coins > 0) {
                (coinsGained * 100.0 / account.currentProgress.coins).toFloat()
            } else 100f
            
            binding.coinsGainedValue.text = if (coinsGained > 0) 
                "+${NumberFormat.getIntegerInstance().format(coinsGained)}" 
            else 
                NumberFormat.getIntegerInstance().format(coinsGained)
            binding.coinsGainedPercentage.text = if (coinsPercentage > 0) 
                "(+${String.format("%.1f", coinsPercentage)}%)" 
            else 
                "(${String.format("%.1f", coinsPercentage)}%)"
            binding.coinsGainedPercentage.setTextColor(
                ContextCompat.getColor(requireContext(), 
                    if (coinsGained >= 0) R.color.chart_line else R.color.delete_background)
            )
            
            // Power Points
            val powerPointsGained = totalProgress.powerPoints - account.currentProgress.powerPoints
            val powerPointsPercentage = if (account.currentProgress.powerPoints > 0) {
                (powerPointsGained * 100.0 / account.currentProgress.powerPoints).toFloat()
            } else 100f
            
            binding.powerPointsGainedValue.text = if (powerPointsGained > 0) 
                "+${NumberFormat.getIntegerInstance().format(powerPointsGained)}" 
            else 
                NumberFormat.getIntegerInstance().format(powerPointsGained)
            binding.powerPointsGainedPercentage.text = if (powerPointsPercentage > 0) 
                "(+${String.format("%.1f", powerPointsPercentage)}%)" 
            else 
                "(${String.format("%.1f", powerPointsPercentage)}%)"
            binding.powerPointsGainedPercentage.setTextColor(
                ContextCompat.getColor(requireContext(), 
                    if (powerPointsGained >= 0) R.color.chart_line else R.color.delete_background)
            )
        } else {
            // Hide progression sections if no progress history
            binding.creditsGainedLabel.visibility = View.GONE
            binding.creditsGainedValue.visibility = View.GONE
            binding.creditsGainedPercentage.visibility = View.GONE
            
            binding.coinsGainedLabel.visibility = View.GONE
            binding.coinsGainedValue.visibility = View.GONE
            binding.coinsGainedPercentage.visibility = View.GONE
            
            binding.powerPointsGainedLabel.visibility = View.GONE
            binding.powerPointsGainedValue.visibility = View.GONE
            binding.powerPointsGainedPercentage.visibility = View.GONE
        }
    }
    
    private fun setupFutureProgressButton() {
        binding.futureProgressButton.setOnClickListener {
            navigateToFutureProgress()
        }
    }
    
    private fun navigateToFutureProgress() {
        arguments?.getString("accountId")?.let { accountId ->
            val action = AccountDetailFragmentDirections.actionAccountDetailToFutureProgress(accountId)
            findNavController().navigate(action)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 