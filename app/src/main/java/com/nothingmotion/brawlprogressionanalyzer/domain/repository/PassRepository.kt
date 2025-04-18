package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.domain.model.PassRewards

interface PassRepository {
    suspend fun getPassFreeTable(): PassRewards?
    suspend fun getPassPremiumTable(): PassRewards?
    suspend fun getPassPlusTable(): PassRewards?
}