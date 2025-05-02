package com.nothingmotion.brawlprogressionanalyzer.util

import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Progress
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AccountUtils {
    companion object {
        /**
         * Calculates the total progress from all previous progresses combined
         */
        fun calculateTotalProgress(account: Account): Progress? {
            val progressHistory = account.previousProgresses.orEmpty()
            if (progressHistory.isNotEmpty()) {
                return progressHistory.fold(account.currentProgress) { acc, progress ->
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
                        isBoughtRankedPass = acc.isBoughtRankedPass,
                        duration = acc.duration
                    )
                }
            }

            return null
        }

        /**
         * Calculates the number of maxed brawlers (power level 11)
         */
        fun calculateMaxedBrawlers(account: Account): Int {
            return account.account.brawlers.count { it.power >= 11 }
        }

        /**
         * Calculates the percentage of maxed brawlers
         */
        fun calculateMaxedPercentage(account: Account): Float {
            val brawlerCount = account.account.brawlers.size
            val maxedBrawlers = calculateMaxedBrawlers(account)
            
            return if (brawlerCount > 0) {
                (maxedBrawlers.toFloat() / brawlerCount.toFloat()) * 100
            } else {
                0f
            }
        }

        /**
         * Calculates trophy progression
         * Returns a pair of (totalTrophiesGained, trophyPercentageChange)
         */
        fun calculateTrophyProgress(account: Account): Pair<Int, Float> {
            val historyData = account.history.orEmpty()
            
            if (historyData.isEmpty()) {
                return Pair(0, 0f)
            }
            
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
            val trophiesPercentage = if (sortedHistoryData.first().trophies > 0) {
                (netTrophiesGained * 100.0 / sortedHistoryData.first().trophies).toFloat()
            } else 0f
            
            return Pair(totalTrophiesGained, trophiesPercentage)
        }

        /**
         * Calculates brawler count progression
         * Returns a pair of (brawlersGained, brawlersPercentageChange)
         */
        fun calculateBrawlerProgress(account: Account): Pair<Int, Float> {
            val historyData = account.history.orEmpty()
            
            if (historyData.isEmpty()) {
                return Pair(0, 0f)
            }
            
            val oldestPlayer = historyData.minByOrNull { it.createdAt.time }
            
            // Calculate brawler progression
            val oldBrawlerCount = oldestPlayer?.brawlers?.size ?: 0
            val newBrawlerCount = account.account.brawlers.size
            val brawlersGained = newBrawlerCount - oldBrawlerCount
            
            val brawlersPercentage = if (oldBrawlerCount > 0) {
                (brawlersGained * 100.0 / oldBrawlerCount).toFloat()
            } else 0f
            
            return Pair(brawlersGained, brawlersPercentage)
        }

        /**
         * Calculates credits progression
         * Returns a pair of (creditsGained, creditsPercentageChange)
         */
        fun calculateCreditsProgress(account: Account): Pair<Int, Float> {
            val totalProgress = calculateTotalProgress(account) ?: return Pair(0, 0f)
            
            val creditsGained = totalProgress.credits - account.currentProgress.credits
            val creditsPercentage = if (account.currentProgress.credits > 0) {
                (creditsGained * 100.0 / account.currentProgress.credits).toFloat()
            } else 100f
            
            return Pair(creditsGained, creditsPercentage)
        }

        /**
         * Calculates coins progression
         * Returns a pair of (coinsGained, coinsPercentageChange)
         */
        fun calculateCoinsProgress(account: Account): Pair<Int, Float> {
            val totalProgress = calculateTotalProgress(account) ?: return Pair(0, 0f)
            
            val coinsGained = totalProgress.coins - account.currentProgress.coins
            val coinsPercentage = if (account.currentProgress.coins > 0) {
                (coinsGained * 100.0 / account.currentProgress.coins).toFloat()
            } else 100f
            
            return Pair(coinsGained, coinsPercentage)
        }

        /**
         * Calculates power points progression
         * Returns a pair of (powerPointsGained, powerPointsPercentageChange)
         */
        fun calculatePowerPointsProgress(account: Account): Pair<Int, Float> {
            val totalProgress = calculateTotalProgress(account) ?: return Pair(0, 0f)
            
            val powerPointsGained = totalProgress.powerPoints - account.currentProgress.powerPoints
            val powerPointsPercentage = if (account.currentProgress.powerPoints > 0) {
                (powerPointsGained * 100.0 / account.currentProgress.powerPoints).toFloat()
            } else 100f
            
            return Pair(powerPointsGained, powerPointsPercentage)
        }

        /**
         * Formats a percentage with a + sign if positive
         */
        fun formatPercentage(percentage: Float): String {
            return if (percentage > 0) 
                "(+${String.format("%.1f", percentage)}%)" 
            else 
                "(${String.format("%.1f", percentage)}%)"
        }

        /**
         * Formats a number with a + sign if positive
         */
        fun formatChange(value: Int): String {
            return if (value > 0) "+$value" else "$value"
        }

        /**
         * Formats a large number with appropriate commas
         */
        fun formatNumber(value: Int): String {
            return NumberFormat.getIntegerInstance().format(value)
        }

        /**
         * Formats a large number with a + sign if positive
         */
        fun formatLargeChange(value: Int): String {
            return if (value > 0) 
                "+${NumberFormat.getIntegerInstance().format(value)}" 
            else 
                NumberFormat.getIntegerInstance().format(value)
        }

        /**
         * Gets date ranges for trophy chart X axis
         */
        fun getHistoryDateLabels(account: Account): List<String> {
            val historyData = account.history.orEmpty().sortedBy { it.createdAt.time }
            val dateFormat = SimpleDateFormat("MMM yy dd", Locale.getDefault())
            val labels = mutableListOf<String>()
            
            // Add historical data points
            historyData.forEach { player ->
                labels.add(dateFormat.format(Date(player.createdAt.time)))
            }
            
            // Add current data point
            labels.add(dateFormat.format(account.updatedAt))
            
            return labels
        }

        /**
         * Gets trophy values for chart Y axis
         */
        fun getTrophyHistoryValues(account: Account): List<Float> {
            val historyData = account.history.orEmpty().sortedBy { it.createdAt.time }
            val values = mutableListOf<Float>()
            
            // Add historical data points
            historyData.forEach { player ->
                values.add(player.trophies.toFloat())
            }
            
            // Add current data point
            values.add(account.account.trophies.toFloat())
            
            return values
        }

        /**
         * Gets brawler count values for chart Y axis
         */
        fun getBrawlerCountHistoryValues(account: Account): List<Float> {
            val historyData = account.history.orEmpty().sortedBy { it.createdAt.time }
            val values = mutableListOf<Float>()
            
            // Add historical data points
            historyData.forEach { player ->
                values.add(player.brawlers.size.toFloat())
            }
            
            // Add current data point
            values.add(account.account.brawlers.size.toFloat())
            
            return values
        }
    }
}