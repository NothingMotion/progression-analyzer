package com.nothingmotion.brawlprogressionanalyzer.ui.account_detail


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.HttpException
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nothingmotion.brawlprogressionanalyzer.BrawlAnalyzerApp
import com.nothingmotion.brawlprogressionanalyzer.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentAccountDetailBinding
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Brawler
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Language
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Progress
import com.nothingmotion.brawlprogressionanalyzer.domain.model.RarityData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlNinjaRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerRepository
import com.nothingmotion.brawlprogressionanalyzer.ui.accounts.BrawlNinjaViewModel
import com.nothingmotion.brawlprogressionanalyzer.util.AccountUtils
import com.nothingmotion.brawlprogressionanalyzer.util.AssetUtils
import com.nothingmotion.brawlprogressionanalyzer.util.JalaliDateUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class AccountDetailFragment : Fragment() {
    private var _binding: FragmentAccountDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountDetailViewModel by viewModels()
    private val numberFormat = DecimalFormat("#,###")
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private var jalaliUtils = JalaliDateUtils()

    private lateinit var account: Account
    private lateinit var brawlerAdapter: BrawlerAdapter

    private var iconLoadingJob: kotlinx.coroutines.Job? = null

    @Inject
    lateinit var brawlerRepository: BrawlerRepository
//    @Inject
//    lateinit var brawlNinjaRepository: BrawlNinjaRepository

    @Inject
    lateinit var prefManager: PreferencesManager
    private val brawlNinjaViewModel: BrawlNinjaViewModel by viewModels()
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
        setupBrawlersList()
        setupRaritiesAccordion()
        setupResourcesAccordion()
        setupStaticIcons()
        // Get accountId from arguments
        arguments?.getString("accountId")?.let { accountId ->
            observeAccount(accountId)
            setupRetryButton(accountId)
        }

//        setupEditButton()
        setupFutureProgressButton()
        setupRefreshAccountButton()
        setupBackButtons()
    }

    private fun setupStaticIcons() {
        AccountDetailHelper.setupStaticIcons(binding, requireContext(), lifecycleScope)
    }


    private fun setupBackButtons() {
        val stopAccountJob = { viewModel.stop() }
        binding.backButton.setOnClickListener { stopAccountJob(); findNavController().popBackStack() }
        binding.backLoadingButton.setOnClickListener { stopAccountJob(); findNavController().popBackStack() }
    }


    private fun setupRetryButton(accountId: String) {
        binding.retryButton.setOnClickListener {
            viewModel.getAccount(accountId)
        }
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
        binding.cameraButton.setOnClickListener {
            photoAccountDialog()
        }
        // Apply custom padding to toolbar
        adjustTitleAppearance()
    }

    private fun photoAccountDialog() {
        AccountPhotoDialogHelper.photoAccountDialog(requireContext(), account.account.tag)
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

    private fun setupBrawlersList() {
        brawlerAdapter = BrawlerAdapter(brawlNinjaViewModel)
        binding.brawlersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = brawlerAdapter
            isNestedScrollingEnabled = true
            setHasFixedSize(true)

            // Set RecyclerView to handle its own scrolling
            isFocusable = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        // Set up listener to scroll to the top when list is updated
        brawlerAdapter.setOnListUpdatedListener(object : BrawlerAdapter.OnListUpdatedListener {
            override fun onListUpdated() {
                binding.brawlersRecyclerView.scrollToPosition(0)
            }
        })

        // Setup search and sort functionality
        setupBrawlerSearch()
        setupBrawlerSort()
    }

    private fun setupBrawlerSearch() {
        // Use a debounce for search to prevent rapid filtering on every keystroke
        var searchJob: kotlinx.coroutines.Job? = null

        binding.searchBrawlersInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Cancel previous job if it exists
                searchJob?.cancel()

                // Create a new job with 300ms delay to prevent rapid filtering
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    kotlinx.coroutines.delay(300) // 300ms debounce
                    brawlerAdapter.filterByName(s.toString())

                    // Check if we have no results to display the empty state
                    binding.noBrawlersFound.visibility = if (brawlerAdapter.hasNoResults()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupBrawlerSort() {
        // Set initial selection
        binding.chipTrophies.isChecked = true

        // Handle chip selection for sorting
        binding.sortChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chip_trophies -> brawlerAdapter.sortBy(BrawlerAdapter.SortType.TROPHIES)
                    R.id.chip_power -> brawlerAdapter.sortBy(BrawlerAdapter.SortType.POWER)
                    R.id.chip_rank -> brawlerAdapter.sortBy(BrawlerAdapter.SortType.RANK)
                    R.id.chip_name -> brawlerAdapter.sortBy(BrawlerAdapter.SortType.NAME)
                }

                // Reset search input when sorting
                binding.searchBrawlersInput.setText("")

                // Use smooth scrolling for a better UX when sort button is clicked
                binding.brawlersRecyclerView.smoothScrollToPosition(0)

                // Hide no results view
                binding.noBrawlersFound.visibility = View.GONE
            }
        }
    }

    private fun observeAccount(accountId: String) {
        // Load account details
        viewModel.getAccount(accountId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    if (state.loading) {
                        binding.loadingText.text = "Loading $accountId.."

                        binding.fabFutureProgress.visibility = View.GONE
                        binding.appBar.visibility = View.GONE
                        binding.accountDetailsGroup.visibility = View.GONE
                        binding.errorStateGroup.visibility = View.GONE

                        binding.loadingAccountsGroup.apply {
                            if (binding.errorStateGroup.isVisible) {
                                binding.errorStateGroup.apply {
                                    applyAnimation(
                                        this,
                                        false,
                                        Runnable {
                                            applyAnimation(
                                                binding.loadingAccountsGroup,
                                                true
                                            )
                                        })
                                }
                            } else {

                                applyAnimation(this, true)
                            }

                        }
                    } else if (state.error != null) {
                        binding.errorMessageText.text = state.error

                        binding.loadingAccountsGroup.visibility = View.GONE
                        binding.appBar.visibility = View.GONE
                        binding.accountDetailsGroup.visibility = View.GONE
                        binding.fabFutureProgress.visibility = View.GONE

                        binding.errorStateGroup.apply {
                            if (binding.loadingAccountsGroup.isVisible) {
                                binding.loadingAccountsGroup.apply {
                                    applyAnimation(
                                        this,
                                        false,
                                        Runnable { applyAnimation(binding.errorStateGroup, true) })
                                }
                            } else {
                                applyAnimation(this, true)

                            }
                        }
                    } else if (state.refreshMessage != null) {
                        Snackbar.make(
                            requireContext(),
                            binding.root,
                            state.refreshMessage,
                            Snackbar.LENGTH_SHORT
                        )
                            .setAction("OK") { }
                            .show()
                    } else {
                        Timber.tag("AccountDetailFragment").d("Calling account")

                        if (state.filteredBrawlerRarity != null && ::account.isInitialized) {
                            Timber.tag("AccountDetailFragment").d("Calling filteredBrawlerRarity")
                            setupRaritiesContent(state.filteredBrawlerRarity)
                            for (rarityPair in state.filteredBrawlerRarity) {
                                val rarity = rarityPair.first
                                val brawlers = rarityPair.second
                                Timber.tag("AccountDetailFragment")
                                    .d("rarity: ${rarity.name}, brawlers: ${brawlers.map { it.name }}")
                            }
                        }

                            state.account?.let {
//                            account?.let{account->if(account==it)return@collectLatest}
//                            binding.loadingAccountsGroup.visibility = View.GONE
                                binding.errorStateGroup.visibility = View.GONE

                                if (binding.loadingAccountsGroup.isVisible)
                                    binding.loadingAccountsGroup.apply {
                                        applyAnimation(this, false, Runnable {

                                            binding.appBar.apply { applyAnimation(this, true) }
                                            binding.accountDetailsGroup.apply {
                                                applyAnimation(
                                                    this,
                                                    true
                                                )
                                            }
                                        })
                                    }
                                binding.fabFutureProgress.visibility = View.VISIBLE
                                account = it
                                updateUI(it)
                            }


                    }
                }
            }
        }
    }

    private fun applyAnimation(
        view: View,
        isIn: Boolean = true,
        withEndAction: Runnable = Runnable { }
    ) {
        AccountDetailHelper.applyAnimation(view, isIn, withEndAction)
    }

    private fun updateUI(account: Account) {
        with(binding) {
            iconLoadingJob?.cancel()
            // Header information
            accountName.text = account.account.name
            accountTag.text = account.account.tag
            accountLevel.text = getString(R.string.level_format, account.account.level)
            // Clear any previous images first to prevent flashing of wrong images
            Glide.with(requireContext()).clear(accountAvatar)

            // Create standard request options for all icon loads
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original & resized image
//                .circleCrop()

            account.account.icon?.id?.let { iconId ->


                val imageUrl = BuildConfig.BRAWLIFY_CDN_API_URL + "profile-icons/regular/$iconId.png"
                Glide.with(requireContext())
                    .load(imageUrl)
                    .apply(requestOptions)
                    .signature(ObjectKey(iconId.toString()))
                    .into(accountAvatar)

            }
            // Stats card
            accountTrophies.text = AccountUtils.formatNumber(account.account.trophies)
            accountHighestTrophies.text = AccountUtils.formatNumber(account.account.highestTrophies)

            // Calculate brawler counts from the brawlers list
            val brawlerCount = account.account.brawlers.size
            accountBrawlers.text = brawlerCount.toString()

            // Count maxed brawlers (power level 11)
            val maxedBrawlers = AccountUtils.calculateMaxedBrawlers(account)
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
            val progressPercentage = AccountUtils.calculateMaxedPercentage(account)

            maxedProgress.progress = progressPercentage.toInt()
            progressionPercentage.text = getString(
                R.string.maxed_percentage_format,
                progressPercentage.toInt()
            )

            var lastUpdate: String = "";
            prefManager.language?.let {
                when (it) {
                    Language.ENGLISH -> {
                        lastUpdate = getString(
                            R.string.last_updated_format,
                            dateFormat.format(account.updatedAt)
                        )
                    }

                    Language.PERSIAN -> {
                        lastUpdate = getString(
                            R.string.last_updated_format,
                            JalaliDateUtils.dateToJalali(account.updatedAt)
                        )
                    }
                }
            }
            // Last updated
            lastUpdated.text = lastUpdate

            // Pass account history to adapter for trophy history charts
            brawlerAdapter.setAccountHistory(account.history)

            // Update brawlers list
            brawlerAdapter.submitSortedList(account.account.brawlers)

            // Ensure no results view is hidden on initial load
            noBrawlersFound.visibility = View.GONE


            // Progression history charts
            setupProgressionCharts(account)
            setupProgressSummary(account)

            // Update resources accordion with current progress
            updateResourcesValues(account.currentProgress)

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

        // Get history data labels and values
        val xLabels = AccountUtils.getHistoryDateLabels(account)
        val values = AccountUtils.getTrophyHistoryValues(account)

        // Create entries for trophy history
        val entries = mutableListOf<Entry>()

        // Add data points
        values.forEachIndexed { index, value ->
            entries.add(Entry(index.toFloat(), value))
        }

        // Find min and max values for better Y axis scaling
        val minTrophies = values.minOrNull() ?: 0f
        val maxTrophies = values.maxOrNull() ?: account.account.trophies.toFloat()
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
                    return AccountUtils.formatNumber(value.toInt())
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

        // Get history data labels and values
        val xLabels = AccountUtils.getHistoryDateLabels(account)
        val values = AccountUtils.getBrawlerCountHistoryValues(account)

        // Create entries for brawler count history
        val entries = mutableListOf<Entry>()

        // Add data points
        values.forEachIndexed { index, value ->
            entries.add(Entry(index.toFloat(), value))
        }

        // Find min and max values for better Y axis scaling
        val maxBrawlers = values.maxOrNull() ?: account.account.brawlers.size.toFloat()
        val minBrawlers = values.minOrNull() ?: 0f
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
            fillAlpha = 85
            setDrawFilled(true)

            // Highlight
            highLightColor = ContextCompat.getColor(requireContext(), R.color.chart_bar_1)
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
    private fun setupLineChart(
        chart: LineChart,
        xLabels: List<String>,
        yAxisMin: Float,
        yAxisMax: Float
    ) {
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
    private fun setupBarChart(
        chart: BarChart,
        xLabels: List<String>,
        yAxisMin: Float,
        yAxisMax: Float
    ) {
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
        binding.fabFutureProgress.setOnClickListener {
            // Get the current account ID from the ViewModel
            viewModel.state.value?.let { account ->
                // Show refreshing message
                Toast.makeText(context, "Moving to future progress...", Toast.LENGTH_SHORT).show()
                navigateToFutureProgress()
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
            append("Trophies: ${AccountUtils.formatNumber(account.account.trophies)}\n")
            append("Highest Trophies: ${AccountUtils.formatNumber(account.account.highestTrophies)}\n")

            // Calculate brawlers and maxed brawlers counts
            val brawlerCount = account.account.brawlers.size
            val maxedBrawlers = AccountUtils.calculateMaxedBrawlers(account)
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
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Account Info", text)
        clipboardManager.setPrimaryClip(clipData)

        // Show confirmation
        Toast.makeText(context, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
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
            val (totalTrophiesGained, trophiesPercentage) = AccountUtils.calculateTrophyProgress(
                account
            )

            // Set trophy values
            binding.trophiesGainedValue.text = AccountUtils.formatLargeChange(totalTrophiesGained)
            binding.trophiesGainedPercentage.text =
                AccountUtils.formatPercentage(trophiesPercentage)

            // Calculate net gain/loss for color
            val netTrophiesGained =
                account.account.trophies - historyData.minByOrNull { it.createdAt.time }!!.trophies

            // Color based on net gain/loss
            binding.trophiesGainedPercentage.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (netTrophiesGained >= 0) R.color.chart_line else R.color.delete_background
                )
            )

            // Calculate brawler progression
            val (brawlersGained, brawlersPercentage) = AccountUtils.calculateBrawlerProgress(account)

            // Set brawler values
            binding.brawlersGainedValue.text = AccountUtils.formatChange(brawlersGained)
            binding.brawlersGainedPercentage.text =
                AccountUtils.formatPercentage(brawlersPercentage)
            binding.brawlersGainedPercentage.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (brawlersGained >= 0) R.color.chart_line else R.color.delete_background
                )
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
            // Credits
            val (creditsGained, creditsPercentage) = AccountUtils.calculateCreditsProgress(account)

            binding.creditsGainedValue.text = AccountUtils.formatChange(creditsGained)
            binding.creditsGainedPercentage.text = AccountUtils.formatPercentage(creditsPercentage)
            binding.creditsGainedPercentage.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (creditsGained >= 0) R.color.chart_line else R.color.delete_background
                )
            )

            // Coins
            val (coinsGained, coinsPercentage) = AccountUtils.calculateCoinsProgress(account)

            binding.coinsGainedValue.text = AccountUtils.formatLargeChange(coinsGained)
            binding.coinsGainedPercentage.text = AccountUtils.formatPercentage(coinsPercentage)
            binding.coinsGainedPercentage.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (coinsGained >= 0) R.color.chart_line else R.color.delete_background
                )
            )

            // Power Points
            val (powerPointsGained, powerPointsPercentage) = AccountUtils.calculatePowerPointsProgress(
                account
            )

            binding.powerPointsGainedValue.text = AccountUtils.formatLargeChange(powerPointsGained)
            binding.powerPointsGainedPercentage.text =
                AccountUtils.formatPercentage(powerPointsPercentage)
            binding.powerPointsGainedPercentage.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (powerPointsGained >= 0) R.color.chart_line else R.color.delete_background
                )
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

    private fun setupRefreshAccountButton() {
        binding.fabRefreshAccount.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Refreshin ${account.account.tag}..",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.refreshAccount()
        }
    }

    private fun navigateToFutureProgress() {
        arguments?.getString("accountId")?.let { accountId ->
            // Fade out the main content before navigation
            binding.apply {
                // Disable the future progress button to prevent multiple clicks
                fabFutureProgress.isEnabled = false
                Toast.makeText(requireContext(), "Loading..", Toast.LENGTH_SHORT).show()
                // Create a smooth fade transition instead of toggling loading state
                val fadeOut =
                    android.animation.ObjectAnimator.ofFloat(accountDetailsGroup, "alpha", 1f, 0.6f)
                        .apply {
                            duration = 300
                            interpolator = android.view.animation.AccelerateInterpolator()
                        }

                fadeOut.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        // Navigate after the fade completes
                        val action =
                            AccountDetailFragmentDirections.actionAccountDetailToFutureProgress(
                                accountId
                            )
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main) {
                                // Navigate to future progress screen
                                Timber.tag("AccountDetailFragment")
                                    .d("Navigating to Future Progress")
                                findNavController().navigate(action)
                            }
                        }.invokeOnCompletion {

                            accountDetailsGroup.alpha = 1f
                            fabFutureProgress.isEnabled = true
                        }


                        // Reset alpha after navigation is complete (it will be restored when we return)
                    }
                })

                fadeOut.start()
            }
        }
    }


    private fun setupRaritiesContent(data: List<Pair<RarityData,List<Brawler>>>){
        val commons = data.find{ it.first == RarityData.COMMON }?.second?.size ?: 0
        val rare = data.find{it.first == RarityData.RARE}?.second?.size ?: 0

        val superRare = data.find { it.first == RarityData.SUPER_RARE }?.second?.size ?: 0
        val epic = data.find { it.first == RarityData.EPIC }?.second?.size ?: 0
        val mythic = data.find{ it.first == RarityData.MYTHIC }?.second?.size ?: 0
        val legendary = data.find{it.first==RarityData.LEGENDARY}?.second?.size ?: 0

        binding.commonCount.text = commons.toString()
        binding.rareCount.text = rare.toString()
        binding.superRareCount.text = superRare.toString()
        binding.epicCount.text = epic.toString()
        binding.mythicCount.text = mythic.toString()
        binding.legendaryCount.text = legendary.toString()
    }
    private fun setupRaritiesAccordion() {
        val raritiesHeader = binding.raritiesHeader
        val raritiesContent = binding.raritiesContent
        val expandIcon = binding.raritiesExpandIcon

        // Set initial state
        raritiesContent.visibility = View.GONE
        expandIcon.setImageResource(R.drawable.ic_expand_more)

        // Set click listener for the header
        raritiesHeader.setOnClickListener {
            // Toggle visibility with animation
            if (raritiesContent.visibility == View.VISIBLE) {
                // Collapse with animation
                val initialHeight = raritiesContent.height

                // Animation to collapse from current height to 0
                val collapseAnimation = ValueAnimator.ofInt(initialHeight, 0).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Int
                        val layoutParams = raritiesContent.layoutParams
                        layoutParams.height = value
                        raritiesContent.layoutParams = layoutParams
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            raritiesContent.visibility = View.GONE
                            raritiesContent.layoutParams.height =
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                    })
                }

                // Animate icon rotation
                val rotateAnimation = ValueAnimator.ofFloat(180f, 0f).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Float
                        expandIcon.rotation = value
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            expandIcon.setImageResource(R.drawable.ic_expand_more)
                        }
                    })
                }

                collapseAnimation.start()
                rotateAnimation.start()
            } else {
                // Expand with animation
                // First make it visible but with 0 height
                raritiesContent.visibility = View.VISIBLE
                raritiesContent.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        raritiesContent.width,
                        View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val targetHeight = raritiesContent.measuredHeight

                // Start with 0 height
                raritiesContent.layoutParams.height = 0

                // Animation to expand from 0 to target height
                val expandAnimation = ValueAnimator.ofInt(0, targetHeight).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Int
                        val layoutParams = raritiesContent.layoutParams
                        layoutParams.height = value
                        raritiesContent.layoutParams = layoutParams
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            raritiesContent.layoutParams.height =
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                    })
                }

                // Animate icon rotation
                val rotateAnimation = ValueAnimator.ofFloat(0f, 180f).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Float
                        expandIcon.rotation = value
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            expandIcon.setImageResource(R.drawable.ic_expand_less)
                        }
                    })
                }

                expandAnimation.start()
                rotateAnimation.start()
            }
        }


    }

    private fun setupResourcesAccordion() {
        val resourcesHeader = binding.resourcesHeader
        val resourcesContent = binding.resourcesContent
        val expandIcon = binding.resourcesExpandIcon

        // Set initial state
        resourcesContent.visibility = View.GONE
        expandIcon.setImageResource(R.drawable.ic_expand_more)

        // Set click listener for the header
        resourcesHeader.setOnClickListener {
            // Toggle visibility with animation
            if (resourcesContent.visibility == View.VISIBLE) {
                // Collapse with animation
                val initialHeight = resourcesContent.height

                // Animation to collapse from current height to 0
                val collapseAnimation = ValueAnimator.ofInt(initialHeight, 0).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Int
                        val layoutParams = resourcesContent.layoutParams
                        layoutParams.height = value
                        resourcesContent.layoutParams = layoutParams
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            resourcesContent.visibility = View.GONE
                            resourcesContent.layoutParams.height =
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                    })
                }

                // Animate icon rotation
                val rotateAnimation = ValueAnimator.ofFloat(180f, 0f).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Float
                        expandIcon.rotation = value
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            expandIcon.setImageResource(R.drawable.ic_expand_more)
                        }
                    })
                }

                collapseAnimation.start()
                rotateAnimation.start()
            } else {
                // Expand with animation
                // First make it visible but with 0 height
                resourcesContent.visibility = View.VISIBLE
                resourcesContent.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        resourcesContent.width,
                        View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val targetHeight = resourcesContent.measuredHeight

                // Start with 0 height
                resourcesContent.layoutParams.height = 0

                // Animation to expand from 0 to target height
                val expandAnimation = ValueAnimator.ofInt(0, targetHeight).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Int
                        val layoutParams = resourcesContent.layoutParams
                        layoutParams.height = value
                        resourcesContent.layoutParams = layoutParams
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            resourcesContent.layoutParams.height =
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                    })
                }

                // Animate icon rotation
                val rotateAnimation = ValueAnimator.ofFloat(0f, 180f).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Float
                        expandIcon.rotation = value
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            expandIcon.setImageResource(R.drawable.ic_expand_less)
                        }
                    })
                }

                expandAnimation.start()
                rotateAnimation.start()
            }
        }
    }

    private fun updateResourcesValues(progress: Progress) {
        // Update resource values from the current progress
        binding.creditsCount.text = progress.credits.toString()
        binding.coinsCount.text = AccountUtils.formatNumber(progress.coins)
        binding.powerPointsCount.text = AccountUtils.formatNumber(progress.powerPoints)
        binding.gadgetsCount.text = progress.gadgets.toString()
        binding.starPowersCount.text = progress.starPowers.toString()
        binding.gearsCount.text = progress.gears.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 