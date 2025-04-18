package com.nothingmotion.brawlprogressionanalyzer.util

import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeStarrDropTableRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropReward
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropRewards
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarrDropUtils {

    @Inject
    lateinit var starrDropTableRepository: FakeStarrDropTableRepository
    fun predictStarrDrops(amountToOpen: Int) : List<StarrDropRewards>{
            val chances : MutableList<StarrDropRewards> = mutableListOf()
            val tables = starrDropTableRepository.starrDropTable.value

            for (table : StarrDropRewards in tables){
                table.chanceToDrop *= amountToOpen
                for(reward : StarrDropReward in table.rewards){
                    reward.chance *= amountToOpen
                }
                chances.add(table);
            }
            return chances

        }
}