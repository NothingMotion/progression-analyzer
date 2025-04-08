package com.nothingmotion.brawlprogressionanalyzer.data.repository

import com.nothingmotion.brawlprogressionanalyzer.model.BrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.model.RarityData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBrawlerTableRepository {
    private val _brawlerTable =
        MutableStateFlow<List<BrawlerTable>>(generateFakeBrawlerTable())

    val brawlerTable: StateFlow<List<BrawlerTable>> = _brawlerTable.asStateFlow()
    private fun generateFakeBrawlerTable(): List<BrawlerTable> {
        return listOf(
            BrawlerTable(
                rarity = RarityData.COMMON,
                creditsNeeded = 0
            ),
            BrawlerTable(
                RarityData.RARE,
                creditsNeeded = 200
            ),
            BrawlerTable(
                RarityData.SUPER_RARE,
                creditsNeeded = 400
            ),
            BrawlerTable(
                RarityData.EPIC,
                creditsNeeded = 950
            ),
            BrawlerTable(
                RarityData.MYTHIC,
                creditsNeeded = 1900
            ),
            BrawlerTable(
                RarityData.LEGENDARY,
                creditsNeeded = 3800
            ),
        )
    }
}