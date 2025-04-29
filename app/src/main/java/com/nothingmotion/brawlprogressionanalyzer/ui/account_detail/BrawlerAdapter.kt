package com.nothingmotion.brawlprogressionanalyzer.ui.account_detail

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nothingmotion.brawlprogressionanalyzer.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.domain.model.AbilityDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Brawler
import com.nothingmotion.brawlprogressionanalyzer.domain.model.GadgetDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Player
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarPowerDataNinja
import com.nothingmotion.brawlprogressionanalyzer.ui.accounts.BrawlNinjaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying brawler items in a RecyclerView
 */
class BrawlerAdapter constructor(private val brawlNinjaViewModel: BrawlNinjaViewModel) :
    ListAdapter<Brawler, BrawlerAdapter.BrawlerViewHolder>(DIFF_CALLBACK) {

    private var originalList: List<Brawler> = emptyList()
    private var filteredList: List<Brawler> = emptyList()
    private var currentSortType: SortType = SortType.TROPHIES
    private var onListUpdatedListener: OnListUpdatedListener? = null
    private var accountHistory: List<Player>? = null


    private var brawlerIconJob: Job? = null

    interface OnListUpdatedListener {
        fun onListUpdated()
    }

    fun setOnListUpdatedListener(listener: OnListUpdatedListener) {
        this.onListUpdatedListener = listener
    }

    /**
     * Set account history for generating trophy history charts
     */
    fun setAccountHistory(history: List<Player>?) {
        this.accountHistory = history
    }

    enum class SortType {
        TROPHIES, POWER, RANK, NAME
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrawlerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brawler, parent, false)
        return BrawlerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrawlerViewHolder, position: Int) {
        val brawler = getItem(position)
        holder.bind(brawler,brawlNinjaViewModel)

        // Set long click listener to show brawler details popup
        holder.itemView.setOnLongClickListener {
            showBrawlerDetailsPopup(holder.itemView.context, brawler)
            true
        }
    }

    private fun showBrawlerDetailsPopup(context: Context, brawler: Brawler) {
        // Inflate the dialog view
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_brawler_details, null)

        // Bind brawler details to the view
        val brawlerIcon = dialogView.findViewById<ImageView>(R.id.brawler_icon)
        val brawlerName = dialogView.findViewById<TextView>(R.id.brawler_name)
        val brawlerPower = dialogView.findViewById<TextView>(R.id.brawler_power)
        val brawlerRank = dialogView.findViewById<TextView>(R.id.brawler_rank)
        val brawlerTrophies = dialogView.findViewById<TextView>(R.id.brawler_trophies)
        val brawlerHighestTrophies =
            dialogView.findViewById<TextView>(R.id.brawler_highest_trophies)
        val brawlerTrophyChart = dialogView.findViewById<LineChart>(R.id.brawler_trophy_chart)
        val noHistoryText = dialogView.findViewById<TextView>(R.id.no_history_text)

        // Set brawler details
        brawlerName.text = brawler.name
        brawlerPower.text = brawler.power.toString()
        brawlerRank.text = brawler.rank.toString()
        brawlerTrophies.text = NumberFormat.getIntegerInstance().format(brawler.trophies)
        brawlerHighestTrophies.text =
            NumberFormat.getIntegerInstance().format(brawler.highestTrophies)

        // TODO: Set brawler icon when available
//        brawlerIcon.setImageResource(R.color.ability_background)
        brawlerIconJob?.cancel()

        val cornerRadius = context.resources.getDimensionPixelSize(R.dimen.avatar_corner_radius)

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transform(RoundedCorners(cornerRadius))

        brawler.id.also {
            val imageUrl = BuildConfig.BRAWLIFY_CDN_API_URL + "brawlers/borderless/${it}.png"
            Timber.tag("BrawlerAdapter")
                .d("Loaded brawler icon: $imageUrl")
            Glide
                .with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .signature(ObjectKey(it.toString()))
                .into(brawlerIcon)
        }
        // Set up the trophy history chart
        setupBrawlerTrophyChart(context, brawler, brawlerTrophyChart, noHistoryText)

        // Create and show the dialog
        MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setupBrawlerTrophyChart(
        context: Context,
        brawler: Brawler,
        chart: LineChart,
        noHistoryText: TextView
    ) {
        // Clear any existing data
        chart.clear()

        // Check if we have account history
        if (accountHistory.isNullOrEmpty()) {
            chart.visibility = View.GONE
            noHistoryText.visibility = View.VISIBLE
            return
        }

        // Collect trophy history for this specific brawler
        val trophyHistory = mutableListOf<Pair<Date, Int>>()

        // Sort history data by timestamp
        val sortedHistory = accountHistory?.sortedBy { it.createdAt.time } ?: emptyList()

        // Extract trophy data for this brawler from each history entry
        for (player in sortedHistory) {
            val brawlerHistory = player.brawlers.find { it.id == brawler.id }
            if (brawlerHistory != null) {
                trophyHistory.add(Pair(player.createdAt, brawlerHistory.trophies))
            }
        }

        // If we don't have enough history data
        if (trophyHistory.size < 2) {
            chart.visibility = View.GONE
            noHistoryText.visibility = View.VISIBLE
            return
        }

        // We have history, setup chart
        chart.visibility = View.VISIBLE
        noHistoryText.visibility = View.GONE

        // Create entries for trophy history
        val entries = mutableListOf<Entry>()
        val xLabels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())

        // Add historical data points
        trophyHistory.forEachIndexed { index, (date, trophies) ->
            entries.add(Entry(index.toFloat(), trophies.toFloat()))
            xLabels.add(dateFormat.format(date))
        }

        // Add current data point if not already included
        val lastDate = trophyHistory.lastOrNull()?.first
        if (lastDate == null || lastDate.time != Date().time) {
            entries.add(Entry(trophyHistory.size.toFloat(), brawler.trophies.toFloat()))
            xLabels.add(dateFormat.format(Date()))
        }

        // Find min and max values for better Y axis scaling
        val minTrophies = entries.minByOrNull { it.y }?.y ?: 0f
        val maxTrophies = entries.maxByOrNull { it.y }?.y ?: brawler.trophies.toFloat()
        val yAxisRange = maxTrophies - minTrophies
        val yAxisMin = maxOf(0f, minTrophies - (yAxisRange * 0.1f)) // 10% padding below
        val yAxisMax = maxTrophies + (yAxisRange * 0.1f) // 10% padding above

        // Create dataset
        val dataSet = LineDataSet(entries, context.getString(R.string.trophy_history)).apply {
            color = ContextCompat.getColor(context, R.color.chart_line)
            lineWidth = 2.5f
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(context, R.color.chart_point))
            circleRadius = 4f
            circleHoleRadius = 2f
            circleHoleColor = Color.WHITE

            // Values above data points
            setDrawValues(true)
            valueTextSize = 9f
            valueTextColor = ContextCompat.getColor(context, R.color.chart_label)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return NumberFormat.getIntegerInstance().format(value.toLong())
                }
            }

            // Smooth curve and fill
            mode = LineDataSet.Mode.CUBIC_BEZIER
            fillColor = ContextCompat.getColor(context, R.color.chart_line_fill)
            fillAlpha = 85
            setDrawFilled(true)

            // Highlight
            highLightColor = ContextCompat.getColor(context, R.color.chart_point)
            highlightLineWidth = 1.5f
        }

        // Create and set line data
        val lineData = LineData(dataSet)
        chart.data = lineData

        // Customize chart appearance
        setupChartAppearance(context, chart, xLabels, yAxisMin, yAxisMax)
    }

    private fun setupChartAppearance(
        context: Context,
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
            xAxis.textColor = ContextCompat.getColor(context, R.color.chart_label)
            xAxis.textSize = 10f
            xAxis.setDrawAxisLine(true)
            xAxis.axisLineWidth = 1.5f
            xAxis.axisLineColor = ContextCompat.getColor(context, R.color.chart_grid)
            xAxis.yOffset = 10f

            // Left Y-axis setup
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = ContextCompat.getColor(context, R.color.chart_grid)
            axisLeft.gridLineWidth = 0.7f
            axisLeft.textColor = ContextCompat.getColor(context, R.color.chart_label)
            axisLeft.textSize = 10f
            axisLeft.axisMinimum = yAxisMin
            axisLeft.axisMaximum = yAxisMax
            axisLeft.setDrawZeroLine(true)
            axisLeft.zeroLineWidth = 1.5f
            axisLeft.zeroLineColor = ContextCompat.getColor(context, R.color.chart_grid)
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

    /**
     * Sort the list by trophies in descending order
     */
    fun submitSortedList(list: List<Brawler>) {
        originalList = list
        filteredList = list
        sortAndFilter(currentSortType, "")
    }

    /**
     * Filter the brawlers list by search query
     */
    fun filterByName(query: String) {
        sortAndFilter(currentSortType, query)
    }

    /**
     * Sort the list by selected criteria
     */
    fun sortBy(sortType: SortType) {
        currentSortType = sortType
        sortAndFilter(sortType, "")
    }

    /**
     * Apply both sorting and filtering
     */
    private fun sortAndFilter(sortType: SortType, query: String) {
        // First filter by name if query is not empty
        val filtered = if (query.isNotEmpty()) {
            originalList.filter { it.name.contains(query, ignoreCase = true) }
        } else {
            originalList
        }

        filteredList = filtered

        // Then sort by selected criteria
        val sorted = when (sortType) {
            SortType.TROPHIES -> filtered.sortedByDescending { it.trophies }
            SortType.POWER -> filtered.sortedByDescending { it.power }
            SortType.RANK -> filtered.sortedByDescending { it.rank }
            SortType.NAME -> filtered.sortedBy { it.name }
        }

        // Only notify for search, not for sort button clicks as they handle scroll themselves
        if (query.isNotEmpty()) {
            submitList(sorted) {
                onListUpdatedListener?.onListUpdated()
            }
        } else {
            submitList(sorted)
        }
    }

    /**
     * Returns true if the filtered list has no data
     */
    fun hasNoResults(): Boolean {
        return filteredList.isEmpty() && originalList.isNotEmpty()
    }

    class BrawlerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val brawlerIcon: ImageView = itemView.findViewById(R.id.brawler_icon)
        private val brawlerName: TextView = itemView.findViewById(R.id.brawler_name)
        private val brawlerTrophies: TextView = itemView.findViewById(R.id.brawler_trophies)
        private val brawlerHighestTrophies: TextView =
            itemView.findViewById(R.id.brawler_highest_trophies)
        private val brawlerPower: TextView = itemView.findViewById(R.id.brawler_power)
        private val brawlerRank: TextView = itemView.findViewById(R.id.brawler_rank)

        // Ability containers
        private val starPowersContainer: HorizontalScrollView =
            itemView.findViewById(R.id.star_powers_container)
        private val starPowersIcons: LinearLayout = itemView.findViewById(R.id.star_powers_icons)
        private val gadgetsContainer: HorizontalScrollView =
            itemView.findViewById(R.id.gadgets_container)
        private val gadgetsIcons: LinearLayout = itemView.findViewById(R.id.gadgets_icons)
        private val gearsContainer: HorizontalScrollView =
            itemView.findViewById(R.id.gears_container)
        private val gearsIcons: LinearLayout = itemView.findViewById(R.id.gears_icons)

        private val numberFormat = NumberFormat.getIntegerInstance()

        private var brawlerDataLoadingJob: Job? = null
        private var gadgetIconLoadingJobs: MutableList<Job?> = mutableListOf()
        private var pendingAbilityDialog: Triple<Context, String, Boolean>? = null

        private lateinit var brawlNinjaViewModel: BrawlNinjaViewModel
        fun bind(brawler: Brawler,
                 brawlNinjaViewModel: BrawlNinjaViewModel) {
            brawlerDataLoadingJob?.cancel()
            this.brawlNinjaViewModel = brawlNinjaViewModel

            brawlerName.text = brawler.name
            brawlerTrophies.text = numberFormat.format(brawler.trophies)
            brawlerHighestTrophies.text = numberFormat.format(brawler.highestTrophies)
            brawlerPower.text = brawler.power.toString()
            brawlerRank.text = brawler.rank.toString()

            // TODO: Add loading of brawler icon from a remote source or local resources
            brawlerIcon.setImageResource(R.color.ability_background)
            setupBrawlerIcon(brawler, itemView.context, itemView, brawlerIcon)
            // Setup abilities
            setupStarPowers(brawler, itemView.context)
            setupGadgets(brawler, itemView.context)
            setupGears(brawler, itemView.context)
        }

        private fun setupBrawlerIcon(
            brawler: Brawler,
            context: Context,
            itemView: View,
            brawlerIcon: ImageView,
        ) {
            Glide.with(context).clear(itemView)

            val cornerRadius = context.resources.getDimensionPixelSize(R.dimen.avatar_corner_radius)
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(RoundedCorners(cornerRadius))

            brawler.id.also {
                val imageUrl = BuildConfig.BRAWLIFY_CDN_API_URL + "brawlers/borderless/${it}.png"
                Timber.tag("BrawlerViewHolder")
                    .d("Loaded brawler icon: $imageUrl")
                Glide
                    .with(context)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .signature(ObjectKey(it.toString()))
                    .into(brawlerIcon)
            }
        }

        private fun setupStarPowers(brawler: Brawler, context: Context) {
            if (brawler.starPowers.isNullOrEmpty()) {
                starPowersContainer.visibility = View.GONE
                return
            }

            starPowersContainer.visibility = View.VISIBLE
            starPowersIcons.removeAllViews()

            brawler.starPowers?.forEach { starPower ->
                // Create star power icon
                val icon = createAbilityIcon(context)
                icon.setImageResource(R.drawable.ic_star_power_placeholder)
                starPowersIcons.addView(icon)
                CoroutineScope(Dispatchers.Main).launch {
                    Timber.tag("BrawlerViewHolder").d("Loading star power icon: ${starPower.id}")
                    val imageUrl =
                        BuildConfig.BRAWLIFY_CDN_API_URL + "star-powers/borderless/${starPower.id}.png"
                    Timber.tag("BrawlerViewHolder").d("Loaded star power icon: ${imageUrl}")
                    Glide.with(context)
                        .load(imageUrl)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(icon)
                }
                
                icon.setOnClickListener {
                    createAbilityListener(context, brawler.name, starPower.id.toString(), false)
                }
                
                // TODO: Set actual star power icon from a remote source or local resources
                icon.contentDescription = starPower.name
            }
        }

        private fun setupGadgets(brawler: Brawler, context: Context) {
            if (brawler.gadgets.isNullOrEmpty()) {
                gadgetsContainer.visibility = View.GONE
                return
            }
            gadgetsContainer.visibility = View.VISIBLE
            gadgetsIcons.removeAllViews()

            brawler.gadgets?.forEach { gadget ->
                // Create parent container

                // Create gadget icon
                val icon = createAbilityIcon(context)
                // Add parent container to gadgetsIcons
                gadgetsIcons.addView(icon)

                gadgetIconLoadingJobs.forEach { it?.cancel() }
                    .also { gadgetIconLoadingJobs.clear() }

                Timber.tag("BrawlerViewHolder").d("Loading gadget icon: ${gadget.id}")

                val imageUrl =
                    BuildConfig.BRAWLIFY_CDN_API_URL + "gadgets/borderless/${gadget.id}.png"

                Timber.tag("BrawlerViewHolder").d("Loaded gadget icon: ${imageUrl}")

                if (adapterPosition != RecyclerView.NO_POSITION)
                    Glide.with(context)
                        .load(imageUrl)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .signature(ObjectKey(gadget.id.toString()))
                        .into(icon)

                icon.setOnClickListener {
                    createAbilityListener(context, brawler.name, gadget.id.toString(), true)
                }

                icon.contentDescription = gadget.name

            }
            // TODO: Set actual gadget icon from a remote source or local resources
        }


        private fun setupGears(brawler: Brawler, context: Context) {
            if (brawler.gears.isNullOrEmpty()) {
                gearsContainer.visibility = View.GONE
                return
            }

            gearsContainer.visibility = View.VISIBLE
            gearsIcons.removeAllViews()

            brawler.gears?.forEach { gear ->
                // Create gear icon
                val icon = createAbilityIcon(context)
                icon.setImageResource(R.drawable.ic_gear_placeholder)
                gearsIcons.addView(icon)


                CoroutineScope(Dispatchers.Main).launch {
                    Timber.tag("BrawlerViewHolder").d("Loading gear icon: ${gear.id}")
                    val imageUrl =
                        BuildConfig.BRAWLIFY_CDN_API_URL + "gears/regular/${gear.id}.png"
                    Timber.tag("BrawlerViewHolder").d("Loaded gear icon: ${imageUrl}")
                    Glide.with(context)
                        .load(imageUrl)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(icon)
                }
                // TODO: Set actual gear icon from a remote source or local resources
                icon.contentDescription = gear.name
            }
        }

        private fun createAbilityIcon(context: Context): ImageView {
            val icon = ImageView(context)
            val size = context.resources.getDimensionPixelSize(R.dimen.ability_icon_size)
            val marginEnd = context.resources.getDimensionPixelSize(R.dimen.ability_icon_margin)

            val layoutParams = LinearLayout.LayoutParams(size, size)
            layoutParams.marginEnd = marginEnd

            icon.layoutParams = layoutParams
            icon.scaleType = ImageView.ScaleType.CENTER_CROP
            icon.contentDescription = context.getString(R.string.ability_icon_description)

            // Make the icon clickable and add ripple effect
            icon.isClickable = true
            icon.isFocusable = true
            
            // Set a background with ripple effect
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            icon.setBackgroundResource(outValue.resourceId)
            
            // Add padding to ensure the ripple is visible
            val padding = context.resources.getDimensionPixelSize(R.dimen.ability_icon_margin) / 2
            icon.setPadding(padding, padding, padding, padding)
            
            return icon
        }
        
        private var currentAbilityDialog: androidx.appcompat.app.AlertDialog? = null
        
        private fun createAbilityListener(context: Context, brawlerName: String, abilityId: String, isGadget: Boolean) {
            // Create and show the dialog with loading state
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_ability_details, null)
            
            // Get references to the groups and views
            val loadingGroup = dialogView.findViewById<androidx.constraintlayout.widget.Group>(R.id.loading_group)
            val errorGroup = dialogView.findViewById<androidx.constraintlayout.widget.Group>(R.id.error_group)
            val contentGroup = dialogView.findViewById<androidx.constraintlayout.widget.Group>(R.id.content_group)
            
            // Error views
            val errorText = dialogView.findViewById<TextView>(R.id.error_text)
            val retryButton = dialogView.findViewById<Button>(R.id.retry_button)
            
            // Content views
            val abilityIcon = dialogView.findViewById<ImageView>(R.id.ability_icon)
            val abilityName = dialogView.findViewById<TextView>(R.id.ability_name)
            val abilityType = dialogView.findViewById<TextView>(R.id.ability_type)
            val abilityDescription = dialogView.findViewById<TextView>(R.id.ability_description)
            
            // Set initial state to loading
            loadingGroup.visibility = View.VISIBLE
            errorGroup.visibility = View.GONE
            contentGroup.visibility = View.GONE
            
            // Create the dialog
            val dialog = MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
            
            // Store the current dialog
            currentAbilityDialog?.dismiss()
            currentAbilityDialog = dialog
            
            // Show the dialog
            dialog.show()
            
            // Set up retry button click listener
            retryButton.setOnClickListener {
                // Explicitly update UI to loading state
                loadingGroup.visibility = View.VISIBLE
                errorGroup.visibility = View.GONE
                contentGroup.visibility = View.GONE
                
                // Add a small delay to ensure UI updates before network request
                CoroutineScope(Dispatchers.Main).launch {
                    // Log the retry attempt
                    Timber.tag("BrawlerAdapter").d("Retry button clicked for brawler: $brawlerName")
                    
                    // Retry fetching the data with a special prefix to bypass random errors
                    brawlNinjaViewModel.getBrawler(context.applicationContext,"retry_$brawlerName")
                }
            }
            
            // First try to get from cache

            val appContext = context.applicationContext
            val brawlerData = brawlNinjaViewModel.getBrawlerFromCache(appContext,brawlerName)

            if (brawlerData != null) {
                // Find the ability based on the type
                val ability = if (isGadget) {
                    brawlerData.gadgets.find { it.id == abilityId }
                } else {
                    brawlerData.starpowers.find { it.id == abilityId }
                }

                // Update dialog if ability found
                ability?.let {
                    updateDialogWithAbility(it, abilityIcon, abilityName, abilityType, abilityDescription, loadingGroup, contentGroup)
                    return
                }
            }

            // Set up state collection when we need to fetch data
            setupStateCollection(context, brawlerName, abilityId, isGadget, dialogView)
            
            // If we couldn't find the ability in cache, fetch the brawler data
            brawlNinjaViewModel.getBrawler(context.applicationContext,brawlerName)
        }
        
        private fun updateDialogWithAbility(
            ability: AbilityDataNinja,
            abilityIcon: ImageView,
            abilityName: TextView,
            abilityType: TextView,
            abilityDescription: TextView,
            loadingGroup: androidx.constraintlayout.widget.Group,
            contentGroup: androidx.constraintlayout.widget.Group
        ) {
            // Set ability details
            abilityName.text = ability.name
            abilityDescription.text = ability.description
            
            // Set ability type
            when (ability) {
                is GadgetDataNinja -> {
                    abilityType.text = "Gadget"
                    // Load gadget icon
                    val imageUrl = BuildConfig.BRAWLIFY_CDN_API_URL + "gadgets/borderless/${ability.id}.png"
                    Glide.with(abilityIcon.context)
                        .load(imageUrl)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(abilityIcon)
                }
                is StarPowerDataNinja -> {
                    abilityType.text = "Star Power"
                    // Load star power icon
                    val imageUrl = BuildConfig.BRAWLIFY_CDN_API_URL + "star-powers/borderless/${ability.id}.png"
                    Glide.with(abilityIcon.context)
                        .load(imageUrl)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(abilityIcon)
                }
                else -> {
                    abilityType.text = "Ability"
                }
            }
            
            // Switch to content state
            loadingGroup.visibility = View.GONE
            contentGroup.visibility = View.VISIBLE
        }
        
        private fun setupStateCollection(context: Context, brawlerName: String, abilityId: String, isGadget: Boolean, dialogView: View) {
            // Get references to the groups and views
            val loadingGroup = dialogView.findViewById<androidx.constraintlayout.widget.Group>(R.id.loading_group)
            val errorGroup = dialogView.findViewById<androidx.constraintlayout.widget.Group>(R.id.error_group)
            val contentGroup = dialogView.findViewById<androidx.constraintlayout.widget.Group>(R.id.content_group)
            
            // Error views
            val errorText = dialogView.findViewById<TextView>(R.id.error_text)
            
            // Content views
            val abilityIcon = dialogView.findViewById<ImageView>(R.id.ability_icon)
            val abilityName = dialogView.findViewById<TextView>(R.id.ability_name)
            val abilityType = dialogView.findViewById<TextView>(R.id.ability_type)
            val abilityDescription = dialogView.findViewById<TextView>(R.id.ability_description)
            
            CoroutineScope(Dispatchers.Main).launch {
                brawlNinjaViewModel.state.collectLatest { state ->

                    Timber.tag("BrawlerViewHolder").d("State: $state")
                    when (state) {
                        is BrawlNinjaViewModel.BrawlNinjaState.Success -> {
                            // Check if this is the brawler we're waiting for
                            if (state.data.name.equals(brawlerName, ignoreCase = true)) {
                                // Find the ability based on the type
                                val ability = if (isGadget) {
                                    state.data.gadgets.find { it.id == abilityId }
                                } else {
                                    state.data.starpowers.find { it.id == abilityId }
                                }
                                
                                // Update dialog if ability found
                                ability?.let {
                                    loadingGroup.visibility = View.GONE
                                    updateDialogWithAbility(it, abilityIcon, abilityName, abilityType, abilityDescription, loadingGroup, contentGroup)
                                    // Cancel this coroutine since we've shown the content
                                } ?: run {
                                    // Ability not found
                                    loadingGroup.visibility = View.GONE
                                    errorGroup.visibility = View.VISIBLE
                                    errorText.text = "Ability not found"
                                    this.cancel()
                                }
                            }
                        }
                        is BrawlNinjaViewModel.BrawlNinjaState.Error -> {
                            // Show error state
                            loadingGroup.visibility = View.GONE
                            errorGroup.visibility = View.VISIBLE
                            errorText.text = "Error: ${state.message}"
                            
                            // Log the error
                            Timber.tag("BrawlerAdapter").e("setupStateCollection: Failed to load brawler data: ${state.message}")
                            
                            // Cancel this coroutine since we've handled the error
                        }
                        is BrawlNinjaViewModel.BrawlNinjaState.Loading -> {

                            Timber.tag("BrawlerAdapter").d("setupStateCollection: Loading brawler data")
                            // Keep showing loading state
                            loadingGroup.visibility = View.VISIBLE
                            errorGroup.visibility = View.GONE
                            contentGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
    
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Brawler>() {
            override fun areItemsTheSame(oldItem: Brawler, newItem: Brawler): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Brawler, newItem: Brawler): Boolean {
                return oldItem == newItem
            }
        }
    }
} 