package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake

import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.RarityData
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
                value = 0
            ),
            BrawlerTable(
                RarityData.RARE,
                value = 200
            ),
            BrawlerTable(
                RarityData.SUPER_RARE,
                value = 400
            ),
            BrawlerTable(
                RarityData.EPIC,
                value = 950
            ),
            BrawlerTable(
                RarityData.MYTHIC,
                value = 1900
            ),
            BrawlerTable(
                RarityData.LEGENDARY,
                value = 3800
            ),
        )
    }
}