package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake

import com.nothingmotion.brawlprogressionanalyzer.domain.model.UpgradeTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.UpgradeTableLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUpgradeTableRepository @Inject constructor(){
    private val _upgradeTable =  MutableStateFlow<UpgradeTable>(generateFakeUpgradeTable())
    val upgradeTable = _upgradeTable.asStateFlow()

    private fun generateFakeUpgradeTable() : UpgradeTable {
        return UpgradeTable(levels =
        listOf(
            UpgradeTableLevel(1,0,0,0,0),
            UpgradeTableLevel(2,20,20,20,20),
            UpgradeTableLevel(3,35,30,55,50),
            UpgradeTableLevel(4,75,50,130,100),
            UpgradeTableLevel(5,140,80,270,180),
            UpgradeTableLevel(6,290,130,560,310),
            UpgradeTableLevel(7,480,210,1040,520),
            UpgradeTableLevel(8,800,340,1840,860),
            UpgradeTableLevel(9,1250,550,3090,1410),
            UpgradeTableLevel(10,1875,890,4965,2300),
            UpgradeTableLevel(11,2800,1440,7765,3740),



        )
        )
    }
}