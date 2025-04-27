package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.databinding.ItemAccountBinding
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.util.DateUtils
import android.view.animation.DecelerateInterpolator
import com.bumptech.glide.Glide
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Language
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.nothingmotion.brawlprogressionanalyzer.BrawlAnalyzerApp
import kotlinx.coroutines.isActive
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.nothingmotion.brawlprogressionanalyzer.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.util.AssetUtils
import kotlinx.coroutines.Job

class AccountsAdapter(
    private val onItemClicked: (Account) -> Unit,
    private val onItemLongClicked: (Account) -> Boolean = { false },
    private val preferencesManager: PreferencesManager,
    private val brawlerRepository: BrawlerRepository
) : ListAdapter<Account, AccountsAdapter.ViewHolder>(AccountDiffCallback()) {

    private var lastAnimatedPosition = -1
    private var animationsEnabled = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAccountBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClicked, onItemLongClicked, preferencesManager,brawlerRepository)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = getItem(position)
        holder.bind(account)
        
        // Run enter animation if this position hasn't been animated yet
        if (animationsEnabled && position > lastAnimatedPosition) {
            animateItem(holder.itemView, position)
            lastAnimatedPosition = position
        }
    }

    private fun animateItem(view: View, position: Int) {
        // Initial state - pushed down and slightly scaled
        view.translationY = 50f
        view.alpha = 0f
        view.scaleX = 0.9f
        view.scaleY = 0.9f
        
        // Calculate delay based on position (staggered animation)
        val delay = (position * 75L).coerceAtMost(500L)
        
        // Animate to final state
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(300)
            .setStartDelay(delay)
            .start()
    }
    
    // Call this when adapter is attached to recycler view
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        animationsEnabled = true
    }
    
    // Call this to reset animation state (e.g., when data set changes completely)
    fun resetAnimationState() {
        lastAnimatedPosition = -1
        animationsEnabled = true
    }
    
    // Disable animations (e.g., for configuration changes)
    fun disableAnimations() {
        animationsEnabled = false
    }

    class ViewHolder(
        private val binding: ItemAccountBinding,
        private val onItemClicked: (Account) -> Unit,
        private val onItemLongClicked: (Account) -> Boolean,
        private val preferencesManager: PreferencesManager,
        private val brawlerRepository: BrawlerRepository
    ) : RecyclerView.ViewHolder(binding.root) {

        // Keep track of the current icon loading job to cancel it when necessary
        private var iconLoadingJob: kotlinx.coroutines.Job? = null
        private var staticIconJobs: MutableList<Job?> = mutableListOf()
        fun bind(account: Account) {
            // Cancel the previous loading job if it exists
            iconLoadingJob?.cancel()
            staticIconJobs.forEach { it?.cancel() }.also{staticIconJobs.clear()}

            binding.apply {
                accountName.text = account.account.name
                accountTag.text = account.account.tag
                accountTrophies.text = account.account.trophies.toString()
                accountLevel.text = binding.root.context.getString(R.string.level_format, account.account.level)

                val trophyIconJob = CoroutineScope(Dispatchers.Main).launch {
                    if (isActive && adapterPosition != RecyclerView.NO_POSITION) {
                        val bitmap = AssetUtils.loadImageAsync(
                            itemView.context,
                            "images/icons/icon_trophy_medium.png"
                        )
                        bitmap?.let {
                            accountTrophiesIcon.setImageBitmap(it)
                        }
                    }
                }
                val brawlerIconJob = CoroutineScope(Dispatchers.Main).launch{
                    if (isActive && adapterPosition != RecyclerView.NO_POSITION) {
                        val bitmap = AssetUtils.loadImageAsync(itemView.context,"images/icons/icon_brawler_super_rare.png")
                        bitmap?.let{
                            accountBrawlersIcon.setImageBitmap(it)
                        }
                    }
                }
                staticIconJobs.add(trophyIconJob)
                staticIconJobs.add(brawlerIconJob)
                // Clear any previous images first to prevent flashing of wrong images
//                Glide.with(itemView).clear(accountAvatar)
                
                // Define corner radius (in pixels)
                val cornerRadius = itemView.context.resources.getDimensionPixelSize(R.dimen.avatar_corner_radius)
                
                // Create standard request options for all icon loads with corner radius
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original & resized image
                    .transform(RoundedCorners(cornerRadius)) // Apply rounded corners
                account.account.icon?.id?.let {

                    val imageUrl = BuildConfig.BRAWLIFY_CDN_API_URL +"profile-icons/regular/${it}.png"
                    Timber.tag("AccountAdapter").d("Image URL: $imageUrl")

                    account.account.icon?.id?.let {
                        Glide
                            .with(itemView)
                            .load(imageUrl)
                            .apply(requestOptions)
                            .signature(ObjectKey(it))
                            .into(accountAvatar)
                    }
                }


                
                // Format brawler stats
                val brawlerCount = account.account.brawlers.size
                accountBrawlerCount.text = binding.root.context.getString(
                    R.string.brawler_count_format, 
                    brawlerCount
                )
                
                // Count maxed brawlers (power level 11)
                val maxedBrawlers = account.account.brawlers.count { it.power >= 11 }
                accountMaxedBrawlers.text = binding.root.context.getString(
                    R.string.maxed_brawlers_format,
                    maxedBrawlers,
                    brawlerCount
                )
                
                // Format last updated date
                val lastUpdated = account.updatedAt
                var formattedDate : String = ""
                preferencesManager.language?.let{lang->
                    formattedDate = when(lang){
                        Language.ENGLISH -> DateUtils.formatDate(lastUpdated)
                        Language.PERSIAN -> DateUtils.toPersianFormatted(lastUpdated)
                        else -> DateUtils.formatDate(lastUpdated)
                    }
                }
                accountLastUpdated.text = binding.root.context.getString(
                    R.string.last_updated_format,
                    formattedDate
                )
                
                // Setup mini trophy chart
                setupMiniTrophyChart(account)
                
                // Set click listener
                root.setOnClickListener { onItemClicked(account) }
                
                // Set long click listener
                root.setOnLongClickListener { onItemLongClicked(account) }
            }
        }
        
        private fun setupMiniTrophyChart(account: Account) {
            val chart = binding.trophyMiniChart
            
            // Clear any existing data
            chart.clear()
            
            // Check if we have history data
            val historyData = account.history
            if (historyData.isNullOrEmpty()) {
                // If no history, hide the chart
                chart.visibility = android.view.View.GONE
                return
            }
            
            // Show the chart
            chart.visibility = android.view.View.VISIBLE
            
            // Create entries from history data
            val entries = mutableListOf<Entry>()
            
            // Add historical data points
            historyData.forEachIndexed { index, player ->
                entries.add(Entry(index.toFloat(), player.trophies.toFloat()))
            }
            
            // Add current data point
            entries.add(Entry(historyData.size.toFloat(), account.account.trophies.toFloat()))
            
            // Create dataset
            val dataSet = LineDataSet(entries, "Trophies").apply {
                // Customize dataset appearance
                color = ContextCompat.getColor(binding.root.context, R.color.chart_line)
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                fillColor = ContextCompat.getColor(binding.root.context, R.color.chart_line_fill)
                fillAlpha = 100
                setDrawFilled(true)
            }
            
            // Create and set line data
            val lineData = LineData(dataSet)
            chart.data = lineData
            
            // Customize chart appearance
            chart.apply {
                description.isEnabled = false
                legend.isEnabled = false
                
                // Disable both axes
                xAxis.isEnabled = false
                axisLeft.isEnabled = false
                axisRight.isEnabled = false
                
                // Remove grid lines
                xAxis.setDrawGridLines(false)
                axisLeft.setDrawGridLines(false)
                axisRight.setDrawGridLines(false)
                
                // Disable touch interactions
                setTouchEnabled(false)
                isDragEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                
                // Set padding
                setViewPortOffsets(0f, 0f, 0f, 0f)
                
                // Animate chart when first shown
                animateX(500)
            }
            
            // Refresh chart
            chart.invalidate()
        }
    }

    private class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.account.tag == newItem.account.tag
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }
} 