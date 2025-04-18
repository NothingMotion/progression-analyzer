package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable

interface BrawlerTableRepository {
    suspend fun getBrawlerTable() : BrawlerTable?
}