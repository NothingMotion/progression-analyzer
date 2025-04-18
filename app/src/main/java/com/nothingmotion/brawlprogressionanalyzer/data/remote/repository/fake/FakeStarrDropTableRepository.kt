package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake

import com.nothingmotion.brawlprogressionanalyzer.domain.model.Bling
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerResource
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Coin
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Credit
import com.nothingmotion.brawlprogressionanalyzer.domain.model.GadgetResource
import com.nothingmotion.brawlprogressionanalyzer.domain.model.HyperCharge
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Pin
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PowerPoint
import com.nothingmotion.brawlprogressionanalyzer.domain.model.ProfileIcon
import com.nothingmotion.brawlprogressionanalyzer.domain.model.RarityData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Skin
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Spray
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarPowerResource
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropReward
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.XPDoubler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeStarrDropTableRepository @Inject constructor(){
    private val _starrDropTable = MutableStateFlow<List<StarrDropRewards>>(emptyList())
    val starrDropTable get() = _starrDropTable.asStateFlow()
    
    init {
        _starrDropTable.value = generateStarrDropTable()
    }
    
    private fun generateStarrDropTable(): List<StarrDropRewards> {
        return listOf(
            // StarrDrop - Rare
            StarrDropRewards(
                id = 1,
                name = "StarrDrop",
                rarity = RarityData.RARE,
                chanceToDrop = 0.5f,
                rewards = listOf(
                    StarrDropReward(Coin(amount = 50), 0.419f),
                    StarrDropReward(PowerPoint(amount = 25), 0.326f),
                    StarrDropReward(XPDoubler(amount = 1), 0.209f),
                    StarrDropReward(Bling(amount = 20), 0.023f),
                    StarrDropReward(Credit(amount = 10), 0.023f)
                )
            ),
            
            // StarrDrop - SuperRare
            StarrDropRewards(
                id = 2,
                name = "StarrDrop",
                rarity = RarityData.SUPER_RARE,
                chanceToDrop = 0.28f,
                rewards = listOf(
                    StarrDropReward(Coin(amount = 100), 0.4238f),
                    StarrDropReward(PowerPoint(amount = 50), 0.3311f),
                    StarrDropReward(XPDoubler(amount = 200), 0.1325f),
                    StarrDropReward(Bling(amount = 50), 0.0331f),
                    StarrDropReward(Credit(amount = 30), 0.0331f),
                    StarrDropReward(Pin(amount = 1), 0.0331f),
                    StarrDropReward(Spray(amount = 1), 0.0331f)
                )
            ),
            
            // StarrDrop - Epic
            StarrDropRewards(
                id = 3,
                name = "StarrDrop",
                rarity = RarityData.EPIC,
                chanceToDrop = 0.15f,
                rewards = listOf(
                    StarrDropReward(Coin(amount = 200), 0.2105f),
                    StarrDropReward(PowerPoint(amount = 100), 0.2105f),
                    StarrDropReward(Pin(amount = 1), 0.1579f),
                    StarrDropReward(Spray(amount = 1), 0.1579f),
                    StarrDropReward(XPDoubler(amount = 500), 0.1053f),
                    StarrDropReward(BrawlerResource(amount = 1, rarity = RarityData.RARE), 0.0526f)
                )
            ),
            
            // StarrDrop - Mythic
            StarrDropRewards(
                id = 4,
                name = "StarrDrop",
                rarity = RarityData.MYTHIC,
                chanceToDrop = 0.05f,
                rewards = listOf(
                    StarrDropReward(PowerPoint(amount = 200), 0.1899f),
                    StarrDropReward(GadgetResource(amount = 1), 0.1582f),
                    StarrDropReward(Skin(amount = 1, rarity = RarityData.RARE), 0.1582f),
                    StarrDropReward(Coin(amount = 500), 0.0949f),
                    StarrDropReward(BrawlerResource(amount = 1, rarity = RarityData.SUPER_RARE), 0.0949f),
                    StarrDropReward(BrawlerResource(amount = 1, rarity = RarityData.EPIC), 0.0633f),
                    StarrDropReward(ProfileIcon(amount = 1), 0.0633f),
                    StarrDropReward(Pin(amount = 1), 0.0633f),
                    StarrDropReward(Spray(amount = 1), 0.0633f),
                    StarrDropReward(Pin(amount = 1), 0.0316f),
                    StarrDropReward(BrawlerResource(amount = 1, rarity = RarityData.MYTHIC), 0.0190f)
                )
            ),
            
            // StarrDrop - Legendary
            StarrDropRewards(
                id = 5,
                name = "StarrDrop",
                rarity = RarityData.LEGENDARY,
                chanceToDrop = 0.02f,
                rewards = listOf(
                    StarrDropReward(Skin(amount = 1, rarity = RarityData.SUPER_RARE), 0.3587f),
                    StarrDropReward(StarPowerResource(amount = 1), 0.2717f),
                    StarrDropReward(HyperCharge(amount = 1), 0.1630f),
                    StarrDropReward(BrawlerResource(amount = 1, rarity = RarityData.EPIC), 0.1087f),
                    StarrDropReward(BrawlerResource(amount = 1, rarity = RarityData.MYTHIC), 0.0543f),
                    StarrDropReward(Skin(amount = 1, rarity = RarityData.EPIC), 0.0217f),
                    StarrDropReward(BrawlerResource(amount = 1, rarity = RarityData.LEGENDARY), 0.0217f)
                )
            )
        )
    }
}