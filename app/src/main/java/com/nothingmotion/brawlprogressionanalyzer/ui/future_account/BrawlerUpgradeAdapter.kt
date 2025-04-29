package com.nothingmotion.brawlprogressionanalyzer.ui.future_account

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import com.nothingmotion.brawlprogressionanalyzer.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.R
import timber.log.Timber
import java.text.NumberFormat

/**
 * RecyclerView Adapter for displaying upgradable brawlers
 * Using ListAdapter for efficient DiffUtil implementation with optimized view recycling
 */
class BrawlerUpgradeAdapter : 
    ListAdapter<UpgradableBrawler, BrawlerUpgradeAdapter.BrawlerViewHolder>(BrawlerDiffCallback()) {

    private val numberFormat = NumberFormat.getIntegerInstance()
    
    // Listener for item clicks
    interface OnBrawlerUpgradeClickListener {
        fun onBrawlerUpgradeClick(brawler: UpgradableBrawler)
    }
    
    private var clickListener: OnBrawlerUpgradeClickListener? = null
    
    fun setOnBrawlerUpgradeClickListener(listener: OnBrawlerUpgradeClickListener) {
        this.clickListener = listener
    }
    
    // Shared Glide request options to avoid creating new instances
    private val glideOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .circleCrop()
        .placeholder(R.drawable.ic_brawler_placeholder)
        .error(R.drawable.ic_brawler_placeholder)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrawlerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brawler_upgrade, parent, false) as MaterialCardView
        return BrawlerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrawlerViewHolder, position: Int) {
        val brawler = getItem(position)
        holder.bind(brawler)
        
        // Set click listener for the item
        holder.itemView.setOnClickListener {
            clickListener?.onBrawlerUpgradeClick(brawler)
        }
    }

    inner class BrawlerViewHolder(private val cardView: MaterialCardView) :
        RecyclerView.ViewHolder(cardView) {
        
        // Cache view references to avoid findViewById calls in bind method
        private val brawlerName: TextView = cardView.findViewById(R.id.brawler_name)
        private val brawlerRarity: TextView = cardView.findViewById(R.id.brawler_rarity)
        private val fromPower: TextView = cardView.findViewById(R.id.from_power)
        private val toPower: TextView = cardView.findViewById(R.id.to_power)
        private val brawlerIcon: ImageView = cardView.findViewById(R.id.brawler_icon)
        private val context = cardView.context
        
        // Keep track of current item to prevent unnecessary bindings
        private var currentItem: UpgradableBrawler? = null

        fun bind(upgradable: UpgradableBrawler) {
            // Skip binding if this is the same item (optimization)
            if (currentItem?.name == upgradable.name && 
                currentItem?.from == upgradable.from && 
                currentItem?.to == upgradable.to) return
                
            currentItem = upgradable
            
            // Set text values
            brawlerName.text = upgradable.name
            brawlerRarity.text = "Upgradable"
            fromPower.text = "${upgradable.from}"
            toPower.text = "${upgradable.to}"

            // Set card background color based on upgrade levels
            val upgradeLevel = upgradable.to - upgradable.from
            val backgroundColor = when {
                upgradeLevel >= 5 -> R.color.legendary_color  // Major upgrade (5+ levels)
                upgradeLevel >= 3 -> R.color.epic_color       // Significant upgrade (3-4 levels)
                upgradeLevel >= 2 -> R.color.super_rare_color // Medium upgrade (2 levels)
                else -> R.color.rare_color                    // Minor upgrade (1 level)
            }

            try {
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, backgroundColor))
            } catch (e: Exception) {
                Timber.e("Color resource not found: $backgroundColor", e)
            }

            // Load brawler image from Brawlify CDN
            val sanitizedName = upgradable.name.replace(" ", "_").lowercase()
            val imageUrl = "${BuildConfig.BRAWLIFY_CDN_API_URL}brawlers/avatars/$sanitizedName.png"
            
            Glide.with(context)
                .load(imageUrl)
                .apply(glideOptions)
                .into(brawlerIcon)
        }
        
        fun recycle() {
            // Clear any Glide references when recycled
            Glide.with(context).clear(brawlerIcon)
        }
    }
    
    override fun onViewRecycled(holder: BrawlerViewHolder) {
        holder.recycle()
        super.onViewRecycled(holder)
    }

    /**
     * DiffUtil callback for efficient updates
     */
    class BrawlerDiffCallback : DiffUtil.ItemCallback<UpgradableBrawler>() {
        override fun areItemsTheSame(oldItem: UpgradableBrawler, newItem: UpgradableBrawler): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: UpgradableBrawler, newItem: UpgradableBrawler): Boolean {
            return oldItem.from == newItem.from && oldItem.to == newItem.to
        }
    }
} 