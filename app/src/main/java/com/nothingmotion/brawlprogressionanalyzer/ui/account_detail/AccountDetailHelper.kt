package com.nothingmotion.brawlprogressionanalyzer.ui.account_detail

import android.content.Context
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentAccountDetailBinding
import com.nothingmotion.brawlprogressionanalyzer.util.AssetUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


object AccountDetailHelper {
     fun setupStaticIcons(binding: FragmentAccountDetailBinding,context:Context,scope: CoroutineScope) {
        binding.apply {
            scope.launch {

                val coin = AssetUtils.loadImageAsync(context, "images/icons/icon_coin.png")
                val credit =
                    AssetUtils.loadImageAsync(context, "images/icons/icon_credit.png")
                val powerPoint =
                    AssetUtils.loadImageAsync(context, "images/icons/icon_power_point.png")
                val gadget =
                    AssetUtils.loadImageAsync(context, "images/icons/icon_gadget.png")
                val starPower =
                    AssetUtils.loadImageAsync(context, "images/icons/icon_star_power.png")
                val gear = AssetUtils.loadImageAsync(context, "images/icons/icon_gear.png")

                val trophies = AssetUtils.loadImageAsync(
                    context,
                    "images/icons/icon_trophy_medium.png"
                )
                val highest = AssetUtils.loadImageAsync(
                    context,
                    "images/icons/icon_leaderboards.png"
                )
                val brawlers =
                    AssetUtils.loadImageAsync(context, "images/icons/icon_brawlers.png")
                coin?.let {
                    coinsIcon.setImageBitmap(it)
                }
                credit?.let {
                    creditsIcon.setImageBitmap(it)
                }
                powerPoint?.let {
                    powerPointsIcon.setImageBitmap(it)
                }
                gadget?.let {
                    gadgetsIcon.setImageBitmap(it)
                }
                starPower?.let {
                    starPowersIcon.setImageBitmap(it)
                }
                gear?.let {
                    gearsIcon.setImageBitmap(it)
                }

                trophies?.let {
                    trophiesIcon.setImageBitmap(it)
                }
                highest?.let {
                    highestTrophiesIcon.setImageBitmap(it)
                }
                brawlers?.let {
                    brawlersIcon.setImageBitmap(it)
                    maxedBrawlersIcon.setImageBitmap(it)
                }

            }
        }
    }

    fun applyAnimation(view:View,isIn:Boolean,withEndAction: Runnable = Runnable{}){
        view.apply {
            alpha = if (isIn) 0f else 1f;
            translationY = if (isIn) -50f else 50f;
            visibility = View.VISIBLE
            animate()
                .alpha(if (isIn) 1f else 0f)
                .translationY(if (isIn) 50f else -50f)
                .setDuration(300)
                .withEndAction {
                    if (!isIn) {
                        visibility = View.GONE
                    }
                }
                .withEndAction(withEndAction)
                .start()
        }
    }
}