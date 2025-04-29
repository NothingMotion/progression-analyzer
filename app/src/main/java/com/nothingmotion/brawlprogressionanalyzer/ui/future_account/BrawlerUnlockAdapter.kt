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
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.domain.model.RarityData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.toRarityData
import timber.log.Timber
import java.text.NumberFormat

/**
 * RecyclerView Adapter for displaying unlockable brawlers
 * Using ListAdapter for efficient DiffUtil implementation with optimized view recycling
 */
class BrawlerUnlockAdapter :
    ListAdapter<UnlockableBrawler, BrawlerUnlockAdapter.BrawlerViewHolder>(BrawlerDiffCallback()) {

    private val numberFormat = NumberFormat.getIntegerInstance()
    
    // Shared Glide request options to avoid creating new instances
    private val glideOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .circleCrop()
        .placeholder(R.drawable.ic_brawler_placeholder)
        .error(R.drawable.ic_brawler_placeholder)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrawlerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brawler_unlock, parent, false) as MaterialCardView
        return BrawlerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrawlerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BrawlerViewHolder(private val cardView: MaterialCardView) :
        RecyclerView.ViewHolder(cardView) {
        
        // Cache view references to avoid findViewById calls in bind method
        private val brawlerName: TextView = cardView.findViewById(R.id.brawler_name)
        private val brawlerRarity: TextView = cardView.findViewById(R.id.brawler_rarity)
        private val brawlerCost: TextView = cardView.findViewById(R.id.brawler_cost)
        private val brawlerIcon: ImageView = cardView.findViewById(R.id.brawler_icon)
        private val context = cardView.context
        
        // Keep track of current item to prevent unnecessary bindings
        private var currentItem: UnlockableBrawler? = null

        fun bind(unlockable: UnlockableBrawler) {
            // Skip binding if this is the same item (optimization)
            if (currentItem?.brawler?.id == unlockable.brawler.id) return
            currentItem = unlockable
            
            val brawler = unlockable.brawler
            val cost = unlockable.cost

            // Set text values
            brawlerName.text = brawler.name
            brawlerRarity.text = brawler.rarity.name
            brawlerCost.text = "${numberFormat.format(cost)} Credits"

            // Set card background color based on rarity
            val rarityColor = when (brawler.rarity.toRarityData()) {
                RarityData.COMMON -> R.color.table_row_even
                RarityData.RARE -> R.color.rare_color
                RarityData.SUPER_RARE -> R.color.super_rare_color
                RarityData.EPIC -> R.color.epic_color
                RarityData.MYTHIC -> R.color.mythic_color
                RarityData.LEGENDARY -> R.color.legendary_color
                else -> androidx.appcompat.R.color.primary_dark_material_dark // Fallback color
            }

            try {
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, rarityColor))
            } catch (e: Exception) {
                Timber.e("Color resource not found: $rarityColor", e)
            }

            // Load brawler image with optimized settings
            Glide.with(context)
                .load(brawler.imageUrl)
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
    class BrawlerDiffCallback : DiffUtil.ItemCallback<UnlockableBrawler>() {
        override fun areItemsTheSame(oldItem: UnlockableBrawler, newItem: UnlockableBrawler): Boolean {
            return oldItem.brawler.id == newItem.brawler.id
        }

        override fun areContentsTheSame(oldItem: UnlockableBrawler, newItem: UnlockableBrawler): Boolean {
            return oldItem.cost == newItem.cost && 
                   oldItem.brawler.name == newItem.brawler.name &&
                   oldItem.brawler.rarity == newItem.brawler.rarity
        }
    }
} 