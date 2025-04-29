package com.nothingmotion.brawlprogressionanalyzer.ui.future_account

import java.text.NumberFormat

/**
 * Utility class that generates personalized next steps advice for players
 * based on their account status and projected resources.
 */
object NextStepsGenerator {
    
    private val numberFormat = NumberFormat.getIntegerInstance()
    
    /**
     * Generates a structured advice message for the player based on their progression state
     * @param state The current FutureProgressState containing all progression-related data
     * @param timeframeMonths The selected timeframe in months
     * @return A formatted advice string that can be directly displayed to the user
     */
    fun generateAdvice(state: FutureProgressState, timeframeMonths: Int): String {
        val sb = StringBuilder()
        
        // Add header
        sb.appendLine("📈 PROGRESSION REPORT")
        sb.appendLine("Here's what you can achieve in the next $timeframeMonths month(s):")
        sb.appendLine()
        
        // Add resources summary
        sb.appendLine("🔹 RESOURCE PROJECTION")
        sb.appendLine("• ${numberFormat.format(state.projectedPowerPoints)} Power Points")
        sb.appendLine("• ${numberFormat.format(state.projectedCoins)} Coins")
        sb.appendLine("• ${numberFormat.format(state.projectedCredits)} Credits")
        sb.appendLine()
        
        // Add brawler unlock potential
        if (state.unlockableBrawlers.isNotEmpty()) {
            sb.appendLine("🔹 BRAWLER UNLOCKS")
            val displayCount = minOf(5, state.unlockableBrawlers.size)
            for (i in 0 until displayCount) {
                val brawler = state.unlockableBrawlers[i]
                sb.appendLine("• ${brawler.brawler.name} (${numberFormat.format(brawler.cost)} credits)")
            }
            
            if (state.unlockableBrawlers.size > displayCount) {
                sb.appendLine("• ...and ${state.unlockableBrawlers.size - displayCount} more!")
            }
            sb.appendLine()
        }
        
        // Add upgrade potential
        if (state.upgradableBrawlers.isNotEmpty()) {
            sb.appendLine("🔹 BRAWLER UPGRADES")
            val displayCount = minOf(5, state.upgradableBrawlers.size)
            for (i in 0 until displayCount) {
                val brawler = state.upgradableBrawlers[i]
                sb.appendLine("• ${brawler.name}: Level ${brawler.from} → ${brawler.to}")
            }
            
            if (state.upgradableBrawlers.size > displayCount) {
                sb.appendLine("• ...and ${state.upgradableBrawlers.size - displayCount} more!")
            }
            sb.appendLine()
        }
        
        // Add progression timeline
        sb.appendLine("🔹 MAX PROGRESSION TIMELINE")
        if (state.monthsToMaxPowerPoints > 0) {
            sb.appendLine("• Power Points: ~${state.monthsToMaxPowerPoints} months")
        }
        if (state.monthsToMaxCoins > 0) {
            sb.appendLine("• Coins: ~${state.monthsToMaxCoins} months")
        }
        sb.appendLine()
        
        // Add personalized advice based on account status
        sb.appendLine("🔹 RECOMMENDED NEXT STEPS")
        generateNextStepsAdvice(sb, state)
        
        return sb.toString()
    }
    
    /**
     * Generates personalized next steps advice based on the player's account status
     */
    private fun generateNextStepsAdvice(sb: StringBuilder, state: FutureProgressState) {
        // Check if we have account data to work with
        if (state.account == null) {
            sb.appendLine("• Connect your account to get personalized advice")
            return
        }
        
        // Determine player's primary bottleneck
        val needsFocus = when {
            state.maxedBrawlers < state.totalBrawlers * 0.5 -> "upgrading your current brawlers"
            state.lockedBrawlers.size > state.totalBrawlers * 0.2 -> "unlocking new brawlers"
            else -> "maxing out remaining brawlers"
        }
        
        sb.appendLine("• Focus on $needsFocus for fastest progression")
        
        // Advice based on resource ratios
        if (state.neededPowerPoints > state.neededCoins * 1.5) {
            sb.appendLine("• Prioritize collecting Power Points from Brawl Pass rewards")
        } else if (state.neededCoins > state.neededPowerPoints * 1.5) {
            sb.appendLine("• Prioritize collecting Coins from weekend events")
        }
        
        // Brawl Pass advice
        sb.appendLine("• Complete all Brawl Pass missions for maximum progression speed")
        
        // Starr Drop strategy
        if (state.starrDropRewards.isNotEmpty()) {
            sb.appendLine("• Make sure to collect all Starr Drops (${state.totalDrops} expected)")
        }
        
        // Player type specific advice
        if (state.monthsToMaxPowerPoints > 12 || state.monthsToMaxCoins > 12) {
            sb.appendLine("• Consider the Brawl Pass premium track to accelerate your progression")
        }
    }
    
    /**
     * Example of how to use this generator
     */
    fun getExample(): String {
        return """
        📈 PROGRESSION REPORT
        Here's what you can achieve in the next 3 month(s):

        🔹 RESOURCE PROJECTION
        • 14,500 Power Points
        • 27,800 Coins
        • 180,000 Credits

        🔹 BRAWLER UNLOCKS
        • Amber (1,999 credits)
        • Spike (1,999 credits)
        • Crow (1,999 credits)
        • Sandy (1,999 credits)
        • Leon (1,999 credits)

        🔹 BRAWLER UPGRADES
        • Shelly: Level 9 → 11
        • Colt: Level 8 → 10
        • Jessie: Level 7 → 9
        • Brock: Level 7 → 9
        • Bull: Level 7 → 9

        🔹 MAX PROGRESSION TIMELINE
        • Power Points: ~16 months
        • Coins: ~22 months

        🔹 RECOMMENDED NEXT STEPS
        • Focus on upgrading your current brawlers for fastest progression
        • Prioritize collecting Coins from weekend events
        • Complete all Brawl Pass missions for maximum progression speed
        • Make sure to collect all Starr Drops (270 expected)
        • Consider the Brawl Pass premium track to accelerate your progression
        """.trimIndent()
    }
} 