package com.nothingmotion.brawlprogressionanalyzer.data.repository

import com.nothingmotion.brawlprogressionanalyzer.model.BrawlPassPlusRewards
import com.nothingmotion.brawlprogressionanalyzer.model.BrawlPassRewards
import com.nothingmotion.brawlprogressionanalyzer.model.PassRewards
import com.nothingmotion.brawlprogressionanalyzer.model.StarrDrop
import com.nothingmotion.brawlprogressionanalyzer.model.StarrDropRewards
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class FakePassTableRepository @Inject constructor(){
    private val _passFreeTable = MutableStateFlow<PassRewards>(
        BrawlPassRewards(
            id = 1,
            name = "Free Pass",
            resources = listOf()
        )
    )
    private val _passPremiumTable = MutableStateFlow<BrawlPassRewards>(
        BrawlPassRewards(
            id = 2,
            name = "Premium Pass",
            resources = listOf()
        )
    )

    private val _passPlusTable = MutableStateFlow<BrawlPassPlusRewards>(BrawlPassPlusRewards(
        id = 3,
        name = "Premium Pass Plus",
        resources = listOf()
    ))

    val passFreeTable get() = _passFreeTable
    val passPremiumTable get() = _passPremiumTable

    val passPlusTable get() = _passPlusTable


}