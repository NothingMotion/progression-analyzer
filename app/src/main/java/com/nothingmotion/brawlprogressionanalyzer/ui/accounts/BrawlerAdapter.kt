package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.model.Brawler
import com.nothingmotion.brawlprogressionanalyzer.model.Player
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying brawler items in a RecyclerView
 */
class BrawlerAdapter : ListAdapter<Brawler, BrawlerAdapter.BrawlerViewHolder>(DIFF_CALLBACK) {

    private var originalList: List<Brawler> = emptyList()
    private var filteredList: List<Brawler> = emptyList()
    private var currentSortType: SortType = SortType.TROPHIES
    private var onListUpdatedListener: OnListUpdatedListener? = null
    private var accountHistory: List<Player>? = null

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
        holder.bind(brawler)

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
        val brawlerHighestTrophies = dialogView.findViewById<TextView>(R.id.brawler_highest_trophies)
        val brawlerTrophyChart = dialogView.findViewById<LineChart>(R.id.brawler_trophy_chart)
        val noHistoryText = dialogView.findViewById<TextView>(R.id.no_history_text)

        // Set brawler details
        brawlerName.text = brawler.name
        brawlerPower.text = brawler.power.toString()
        brawlerRank.text = brawler.rank.toString()
        brawlerTrophies.text = NumberFormat.getIntegerInstance().format(brawler.trophies)
        brawlerHighestTrophies.text = NumberFormat.getIntegerInstance().format(brawler.highestTrophies)

        // TODO: Set brawler icon when available
        brawlerIcon.setImageResource(R.color.ability_background)

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

    class BrawlerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val brawlerIcon: ImageView = itemView.findViewById(R.id.brawler_icon)
        private val brawlerName: TextView = itemView.findViewById(R.id.brawler_name)
        private val brawlerTrophies: TextView = itemView.findViewById(R.id.brawler_trophies)
        private val brawlerHighestTrophies: TextView = itemView.findViewById(R.id.brawler_highest_trophies)
        private val brawlerPower: TextView = itemView.findViewById(R.id.brawler_power)
        private val brawlerRank: TextView = itemView.findViewById(R.id.brawler_rank)

        // Ability containers
        private val starPowersContainer: HorizontalScrollView = itemView.findViewById(R.id.star_powers_container)
        private val starPowersIcons: LinearLayout = itemView.findViewById(R.id.star_powers_icons)
        private val gadgetsContainer: HorizontalScrollView = itemView.findViewById(R.id.gadgets_container)
        private val gadgetsIcons: LinearLayout = itemView.findViewById(R.id.gadgets_icons)
        private val gearsContainer: HorizontalScrollView = itemView.findViewById(R.id.gears_container)
        private val gearsIcons: LinearLayout = itemView.findViewById(R.id.gears_icons)

        private val numberFormat = NumberFormat.getIntegerInstance()

        fun bind(brawler: Brawler) {
            brawlerName.text = brawler.name
            brawlerTrophies.text = numberFormat.format(brawler.trophies)
            brawlerHighestTrophies.text = numberFormat.format(brawler.highestTrophies)
            brawlerPower.text = brawler.power.toString()
            brawlerRank.text = brawler.rank.toString()

            // TODO: Add loading of brawler icon from a remote source or local resources
            brawlerIcon.setImageResource(R.color.ability_background)

            // Setup abilities
            setupStarPowers(brawler, itemView.context)
            setupGadgets(brawler, itemView.context)
            setupGears(brawler, itemView.context)
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
                starPowersIcons.addView(icon)
                
                // TODO: Set actual star power icon from a remote source or local resources
                icon.setImageResource(R.drawable.ic_star_power_placeholder)
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
                // Create gadget icon
                val icon = createAbilityIcon(context)
                gadgetsIcons.addView(icon)
                
                // TODO: Set actual gadget icon from a remote source or local resources
                icon.setImageResource(R.drawable.ic_gadget_placeholder)
                icon.contentDescription = gadget.name
            }
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
                gearsIcons.addView(icon)
                
                // TODO: Set actual gear icon from a remote source or local resources
                icon.setImageResource(R.drawable.ic_gear_placeholder)
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
            
            return icon
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