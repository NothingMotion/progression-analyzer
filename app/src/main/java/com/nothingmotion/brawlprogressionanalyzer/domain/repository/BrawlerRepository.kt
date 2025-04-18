package com.nothingmotion.brawlprogressionanalyzer.domain.repository

import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData

interface BrawlerRepository {
    suspend fun getBrawler(id: Long): BrawlerData?
    suspend fun getBrawlers(): List<BrawlerData>
}