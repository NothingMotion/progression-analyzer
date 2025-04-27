package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake

import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Brawler
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Gadget
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Gear
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Player
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Progress
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarPower
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Repository that provides fake account data for testing and development
 */
@Singleton
class FakeAccountRepository @Inject constructor() {

    private val _accounts = MutableStateFlow<List<Account>>(generateFakeAccounts())
    val accounts: Flow<List<Account>> = _accounts.asStateFlow()

    /**
     * Get a specific account by ID
     */
    suspend fun getAccount(id: String): Account? {
        return _accounts.value.find { it.account.tag == id }
    }
    
    /**
     * Refresh accounts from the data source
     * This simulates a network call with artificial delay
     */
    suspend fun refreshAccounts(): List<Account> {
        // Simulate network delay
        delay(5000)
        
        // 10% chance of error for testing error handling
        if (Random.nextInt(0, 10) == 0) {
            throw Exception("Network error: Failed to connect to server")
        }
        
        val accounts = generateFakeAccounts()
        _accounts.value = accounts
        return accounts
    }
    
    suspend fun getAllAccounts(): List<Account>{
        delay(2000)
        return generateFakeAccounts()
    }

    /**
     * Add a new account to the repository
     */
    suspend fun addAccount(account: Account) {
        _accounts.value += account
    }

    /**
     * Delete an account from the repository
     */
    suspend fun deleteAccount(id: String) {
        _accounts.value = _accounts.value.filter { it.account.tag != id }
    }

    /**
     * Update an existing account
     */
    suspend fun updateAccount(account: Account) {
        _accounts.value = _accounts.value.map {
            if (it.account.tag == account.account.tag) account else it
        }
    }

    /**
     * Generates fake account data with history
     */
    private fun generateFakeAccounts(): List<Account> {
        val accounts = mutableListOf<Account>()

        // Fake account 1 - Pro player
        val proPlayerId = UUID.randomUUID().toString()
        val proPlayerAccount = createAccount(
            id = proPlayerId,
            name = "BrawlMaster",
            tag = "#2YQ9VRLJ",
            trophies = 35000,
            highestTrophies = 36000,
            level = 200,
            brawlerCount = 65,
            maxedBrawlersCount = 60,
            historyMonths = 6
        )
        accounts.add(proPlayerAccount)

        // Fake account 2 - Mid-tier player
        val midTierId = UUID.randomUUID().toString()
        val midTierAccount = createAccount(
            id = midTierId,
            name = "StarBrawler",
            tag = "#8VPRG22C",
            trophies = 22000,
            highestTrophies = 24000,
            level = 150,
            brawlerCount = 62,
            maxedBrawlersCount = 35,
            historyMonths = 5
        )
        accounts.add(midTierAccount)

        // Fake account 3 - Beginner player
        val beginnerId = UUID.randomUUID().toString()
        val beginnerAccount = createAccount(
            id = beginnerId,
            name = "BrawlNewbie",
            tag = "#9CQ8VR20",
            trophies = 8000,
            highestTrophies = 8500,
            level = 60,
            brawlerCount = 40,
            maxedBrawlersCount = 5,
            historyMonths = 3
        )
        accounts.add(beginnerAccount)

        // Fake account 4 - newbie
        val newbieId = UUID.randomUUID().toString()
        val newbieAccount = Account(
            account = Player(
//                id = newbieId,
                name = "BrawlNewbie 2", 
                tag = "#2pp", 
                trophies = 4000, 
                highestTrophies = 4200, 
                level = 40, 
                brawlers = generateBrawlers(24, 2)
            ),
            history = null,
            previousProgresses = null,
            currentProgress = createProgress(4000, 200, 2, 24, getDateMonthsAgo(4)),
            futureProgresses = null,
            createdAt = getDateMonthsAgo(4),
            updatedAt = Date()
        )

        accounts.add(newbieAccount)

        return accounts
    }

    /**
     * Creates a fake account with history data
     */
    private fun createAccount(
        id: String,
        name: String,
        tag: String,
        trophies: Int,
        highestTrophies: Int,
        level: Int,
        brawlerCount: Int,
        maxedBrawlersCount: Int,
        historyMonths: Int
    ): Account {
        // Create the current player state with generated brawlers
        val brawlers = generateBrawlers(brawlerCount, maxedBrawlersCount)
        
        val currentPlayer = Player(
//            id = id,
            name = name,
            tag = tag,
            trophies = trophies,
            highestTrophies = highestTrophies,
            level = level,
            brawlers = brawlers,
            createdAt = Date()
        )

        // Generate player history
        val playerHistory = generatePlayerHistory(
            currentPlayer = currentPlayer,
            historyMonths = historyMonths,
            brawlerCount = brawlerCount,
            maxedBrawlersCount = maxedBrawlersCount
        )

        // Generate progress data
        val currentProgress = createProgress(
            coins = Random.nextInt(1000, 5000),
            powerPoints = Random.nextInt(500, 2000),
            brawlerLevel = level / 10,
            brawlerCount = brawlerCount,
            date = Date()
        )

        // Generate progress history
        val progressHistory = generateProgressHistory(
            currentProgress = currentProgress,
            historyMonths = historyMonths
        )

        return Account(
            account = currentPlayer,
            history = playerHistory,
            previousProgresses = progressHistory,
            currentProgress = currentProgress,
            futureProgresses = null,
            createdAt = getDateMonthsAgo(historyMonths),
            updatedAt = Date()
        )
    }

    /**
     * Generate a list of brawlers with specified count and maxed count
     */
    private fun generateBrawlers(brawlerCount: Int, maxedBrawlersCount: Int): List<Brawler> {
        val brawlers = mutableListOf<Brawler>()
        val random = Random(System.currentTimeMillis())
        
        for (i in 1..brawlerCount) {
            val isPowerMax = i <= maxedBrawlersCount
            val power = if (isPowerMax) 11 else Random.nextInt(1, 10)
            val trophies = Random.nextInt(300, 800)
            
            // Create brawler with random data
            val brawler = Brawler(
                id = i.toLong(),
                name = "Brawler $i",
                trophies = trophies,
                highestTrophies = trophies + Random.nextInt(0, 150),
                rank = Random.nextInt(10, 25),
                power = power,
                gears = if (power >= 10) generateGears(Random.nextInt(1, 3)) else null,
                starPowers = if (power >= 9) generateStarPowers(Random.nextInt(1, 2)) else null,
                gadgets = if (power >= 7) generateGadgets(Random.nextInt(1, 2)) else null
            )
            
            brawlers.add(brawler)
        }
        
        return brawlers
    }
    
    /**
     * Generate random gears
     */
    private fun generateGears(count: Int): List<Gear> {
        val gearTypes = listOf("Speed", "Damage", "Shield", "Health", "Vision")
        return List(count) { index ->
            Gear(
                id = index.toLong() + 1,
                name = gearTypes[index % gearTypes.size]
            )
        }
    }
    
    /**
     * Generate random star powers
     */
    private fun generateStarPowers(count: Int): List<StarPower> {
        val powerNames = listOf("Power Surge", "Energize", "Magnum Special", "Shocky", "Snappy Sniping")
        return List(count) { index ->
            StarPower(
                id = index.toLong() + 1,
                name = powerNames[index % powerNames.size]
            )
        }
    }
    
    /**
     * Generate random gadgets
     */
    private fun generateGadgets(count: Int): List<Gadget> {
        val gadgetNames = listOf("Fast Forward", "Speedzone", "Clay Pigeons", "Silver Bullet", "Pulse Emitter")
        return List(count) { index ->
            Gadget(
                id = index.toLong() + 1,
                name = gadgetNames[index % gadgetNames.size]
            )
        }
    }

    /**
     * Generates player history data for the given months
     */
    private fun generatePlayerHistory(
        currentPlayer: Player,
        historyMonths: Int,
        brawlerCount: Int,
        maxedBrawlersCount: Int
    ): List<Player> {
        val history = mutableListOf<Player>()
        val random = Random(currentPlayer.tag.hashCode())

        // We'll create fewer data points for history to keep it simple
        // In a real app, this would be more comprehensive
        for (i in historyMonths downTo 1) {
            val date = getDateMonthsAgo(i)

            // Decrease stats based on how far back we go
            val decreaseFactor = i.toFloat() / historyMonths.toFloat()
            val trophiesDecrease = (currentPlayer.trophies * decreaseFactor * 0.4f).toInt()
            val levelDecrease = (currentPlayer.level * decreaseFactor * 0.3f).toInt()
            val brawlerCountDecrease = (brawlerCount * decreaseFactor * 0.25f).toInt()
            val maxedBrawlersDecrease = (maxedBrawlersCount * decreaseFactor * 0.6f).toInt()

            // Add some randomness
            val trophiesRandom = random.nextInt(-500, 500)
            val levelRandom = random.nextInt(-5, 5)

            // Calculate historical brawler counts
            val historicalBrawlerCount = maxOf(brawlerCount - brawlerCountDecrease, 1)
            val historicalMaxedCount = maxOf(maxedBrawlersCount - maxedBrawlersDecrease, 0)

            // Ensure values stay within reasonable bounds
            val historicalPlayer = Player(
//                id = currentPlayer.id,
                name = currentPlayer.name,
                tag = currentPlayer.tag,
                trophies = maxOf(currentPlayer.trophies - trophiesDecrease + trophiesRandom, 0),
                highestTrophies = maxOf(currentPlayer.highestTrophies - (trophiesDecrease / 2) + trophiesRandom, 0),
                level = maxOf(currentPlayer.level - levelDecrease + levelRandom, 1),
                brawlers = generateBrawlers(historicalBrawlerCount, historicalMaxedCount),
                createdAt = date
            )

            history.add(historicalPlayer)
        }

        return history
    }

    /**
     * Generates progress history data for the given months
     */
    private fun generateProgressHistory(
        currentProgress: Progress,
        historyMonths: Int
    ): List<Progress> {
        val history = mutableListOf<Progress>()
        val random = Random(currentProgress.hashCode())

        for (i in historyMonths downTo 1) {
            val date = getDateMonthsAgo(i)

            // Decrease stats based on how far back we go
            val decreaseFactor = i.toFloat() / historyMonths.toFloat()
            val coinsDecrease = (currentProgress.coins * decreaseFactor * 0.5f).toInt()
            val powerPointsDecrease = (currentProgress.powerPoints * decreaseFactor * 0.6f).toInt()
            val brawlersDecrease = (currentProgress.brawlers * decreaseFactor * 0.3f).toInt()
            val averageLevelDecrease =
                (currentProgress.averageBrawlerPower * decreaseFactor * 0.4f).toInt()

            // Create historical progress
            val historicalProgress = Progress(
                coins = maxOf(currentProgress.coins - coinsDecrease, 0),
                powerPoints = maxOf(currentProgress.powerPoints - powerPointsDecrease, 0),
                credits = Random.nextInt(0, 100),
                gears = Random.nextInt(0, 50),
                starPowers = Random.nextInt(
                    currentProgress.starPowers - 5,
                    currentProgress.starPowers
                ),
                gadgets = Random.nextInt(currentProgress.gadgets - 10, currentProgress.gadgets),
                brawlers = maxOf(currentProgress.brawlers - brawlersDecrease, 1),
                averageBrawlerPower = maxOf(
                    currentProgress.averageBrawlerPower - averageLevelDecrease,
                    1
                ),
                averageBrawlerTrophies = Random.nextInt(300, 500),
                isBoughtPass = i % 2 == 0, // Alternating
                isBoughtPassPlus = i % 3 == 0, // Every third month
                isBoughtRankedPass = false, // Not bought in the past
                duration = date
            )

            history.add(historicalProgress)
        }

        return history
    }

    /**
     * Creates a fake progress record
     */
    private fun createProgress(
        coins: Int,
        powerPoints: Int,
        brawlerLevel: Int,
        brawlerCount: Int,
        date: Date
    ): Progress {
        return Progress(
            coins = coins,
            powerPoints = powerPoints,
            credits = Random.nextInt(100, 500),
            gears = Random.nextInt(50, 200),
            starPowers = Random.nextInt(10, 30),
            gadgets = Random.nextInt(20, 50),
            brawlers = brawlerCount,
            averageBrawlerPower = brawlerLevel,
            averageBrawlerTrophies = Random.nextInt(500, 700),
            isBoughtPass = true,
            isBoughtPassPlus = Random.nextBoolean(),
            isBoughtRankedPass = Random.nextBoolean(),
            duration = date
        )
    }

    /**
     * Returns a date that is the specified number of months ago
     */
    private fun getDateMonthsAgo(months: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -months)
        return calendar.time
    }
} 