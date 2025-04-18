package com.nothingmotion.brawlprogressionanalyzer.data.repository.fake

import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Class
import com.nothingmotion.brawlprogressionanalyzer.domain.model.GadgetData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Rarity
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarPowerData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class FakeBrawlerRepository {
    private val _brawlers = MutableStateFlow<List<BrawlerData>>(generateFakeBrawlers())
    val brawlers: StateFlow<List<BrawlerData>> = _brawlers.asStateFlow()

    private var lastId = 0

    fun getBrawler(id: Int): BrawlerData? {
        return _brawlers.value.find { it.id == id }
    }

    private fun generateFakeBrawlers(): List<BrawlerData> {
        val brawlers = mutableListOf<BrawlerData>()
        for (i in 1..100) {
            brawlers.add(
                createBrawler(
                    id = i,
                    name = "brawler_$i",
                )
            )
        }
        return brawlers
    }

    private fun createBrawler(
        id: Int = 0,
        name: String = "",
//        description: String = "",
    ): BrawlerData {
        lastId ++;
        val random = lastId
        val description = ""
        return BrawlerData(
            id = random,
            avatarId = id * 100,
            `class` = randomClass(),
            description = description,
            descriptionHtml = "<p>$description</p>",
            fankit = "https://example.com/fankit/$id",
            gadgets = randomGadget(),
            hash = "brawler_$id",
            imageUrl = "https://example.com/brawlers/$id.png",
            imageUrl2 = "https://example.com/brawlers/${id}_2.png",
            imageUrl3 = "https://example.com/brawlers/${id}_3.png",
            link = "/brawlers/$name",
            name = "Brawler $random",
            path = "/brawlers/$name",
            rarity = randomRarity(),
            released = true,
            starPowers = randomStarPower(),
            unlock = "Unlock $random",
            version = 1,
            videos = emptyList()
        )
    }
    private fun randomGadget(): List<GadgetData>{
        val gadgets = mutableListOf<GadgetData>()
        for (i in 1..3) {
            gadgets.add(
                GadgetData(
                    description = "Gadget $i",
                    descriptionHtml = "<p>Gadget $i</p>",
                    id = i,
                    imageUrl = "https://example.com/gadgets/$i.png",
                    name = "Gadget $i",
                    path = "/gadgets/$i",
                    released = true,
                    version = 1
                )
            )
        }
        return gadgets
    }


    private fun randomStarPower(): List<StarPowerData>{
        val starPowers = mutableListOf<StarPowerData>()
        for (i in 1..3) {
            starPowers.add(
                StarPowerData(
                    description = "Star Power $i",
                    descriptionHtml = "<p>Star Power $i</p>",
                    id = i,
                    imageUrl = "https://example.com/starpowers/$i.png",
                    name = "Star Power $i",
                    path = "/starpowers/$i",
                    released = true,
                    version = 1
                )
            )
        }
        return starPowers
    }
    private fun randomRarity(): Rarity {
        val random = Random.nextInt(1, 4)
        return when (random) {
            1 -> Rarity(color = "#00FF00", id = 0, name = "Common")
            2 -> Rarity(color = "#0000FF", id = 1, name = "Rare")
            3 -> Rarity(color = "#FF0000", id = 2, name = "Super Rare")
            else -> Rarity(color = "#FF00FF", id = 3, name = "Epic")
        }
    }
    private fun randomClass(): Class {
        val random = Random.nextInt(1, 4)
        return when (random) {
            1 -> Class(id = 1, name = "Damage Dealer")
            2 -> Class(id = 2, name = "Tank")
            3 -> Class(id = 3, name = "Sharpshooter")
            else -> Class(id = 4, name = "Assassin")
        }
    }
} 