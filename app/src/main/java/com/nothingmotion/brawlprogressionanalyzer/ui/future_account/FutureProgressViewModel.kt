package com.nothingmotion.brawlprogressionanalyzer.ui.future_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeBrawlerTableRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakePassTableRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeStarrDropTableRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeUpgradeTableRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Coin
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Credit
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PassRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PowerPoint
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.UpgradeTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.toRarityData
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.NumberFormat
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

@HiltViewModel
class FutureProgressViewModel @Inject constructor(
    private val brawlerTableRepository: FakeBrawlerTableRepository,
    private val upgradeTableRepository: FakeUpgradeTableRepository,
    private val passRepository: FakePassTableRepository,
    private val starrDropRepository: FakeStarrDropTableRepository,
    private val brawlerDataRepository: BrawlerRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

//    private val accountDetailViewModel: AccountDetailViewModel
    private val _state = MutableStateFlow(FutureProgressState())
    val state: StateFlow<FutureProgressState> = _state

    // Resource estimates per month
    private val basePowerPointsPerMonth = 0
    private val baseCoinsPerMonth = 0

    // Starr drop configuration
    private val dropPerDay = 3
    private val passDrops = 29

    // Default timeframe
    private var selectedTimeframeMonths = 1
    private var isP2WPlayer = false

    // Number formatter
    private val numberFormat = NumberFormat.getIntegerInstance()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Load upgrade table
            upgradeTableRepository.upgradeTable.collectLatest { upgradeTable ->
                _state.update { it.copy(upgradeTable = upgradeTable) }
                calculateResourcesNeeded()
                updateFutureResources()
                calculateUpgradableBrawlers()

            }
        }

        viewModelScope.launch {
            // Load brawler table
            brawlerTableRepository.brawlerTable.collectLatest { brawlerTable ->
                _state.update { it.copy(brawlerTable = brawlerTable) }
                calculateUnlockableBrawlers()
            }
        }

        viewModelScope.launch {
            // Load starr drop table
            starrDropRepository.starrDropTable.collectLatest { starrDropTable ->
                _state.update { it.copy(starrDropRewards = starrDropTable) }
                updateFutureResources()

            }
        }


        viewModelScope.launch {



            withContext(Dispatchers.IO){
                brawlerDataRepository.getBrawlers().collectLatest { result ->
                    when(result){
                        is Result.Error -> {
                            Timber.e("Error fetching brawlers: ${result.error}")
                            _state.update { it.copy(brawlersData = emptyList()) }
                        }
                        is Result.Loading -> {
                            Timber.d("Loading brawlers data...")
                            _state.update { it.copy(brawlersData = emptyList()) }
                        }
                        is Result.Success -> {
                            _state.update { it.copy(brawlersData = result.data) }
                            calculateUnlockableBrawlers()
                            calculateUpgradableBrawlers()

                        }
                    }

                }

            }
            // Load brawlers data
        }
    }

    fun getAccount(accountId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                when(val result = accountRepository.getAccount(accountId,"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI4M2E0Y2M3YS01OTdiLTQ1YWEtYjg0Ny0yOTg3MjVmYWEwZDkiLCJpYXQiOjE3NDU4MTkzMjgsImV4cCI6MTc0NTkwNTcyOH0.-QfXTb0sF8eqgu_m-SajtCcCsPSR3X_mvvzh4VoeGTs")){
                    is Result.Error -> {

                        Timber.e("Error fetching account: ${result.error}")
                        _state.update { it.copy(account = null) }
                    }
                    is Result.Loading -> {
                        Timber.d("Loading account data...")
                        _state.update { it.copy(account = null) }
                    }
                    is Result.Success -> {

                        result.data.account?.let { account ->
                            _state.update { it.copy(account = result.data) }
                            calculateResourcesNeeded()
                            updateFutureResources()
                            calculateUnlockableBrawlers()
                            calculateUpgradableBrawlers()

                        }
                    }
                }

            }
        }
    }

    /**
     * Calculate power points and coins needed to max out all brawlers
     */
    private fun calculateResourcesNeeded() {
        val account = _state.value.account ?: return
        val upgradeTable = _state.value.upgradeTable ?: return

        // Calculate brawler counts from the brawlers list
        val brawlerCount = account.account.brawlers.size
        val maxedBrawlers = account.account.brawlers.count { it.power >= 11 }
        val nonMaxedBrawlers = brawlerCount - maxedBrawlers

        // Assuming a brawler is "maxed" at level 11
        // We need to calculate the total power points and coins to get from current average level to max
        val currentAveragePower = account.currentProgress.averageBrawlerPower

        var totalPowerPointsNeeded = 0
        var totalCoinsNeeded = 0

        // Calculate resources needed per non-maxed brawler
        if (currentAveragePower < 11) {
            // Find the upgrade level that matches the current average power
            val currentLevel = upgradeTable.levels.find { it.level == currentAveragePower }
                ?: upgradeTable.levels.first()

            // Get total resources needed from current level to max (level 11)
            val maxLevel = upgradeTable.levels.last()

            // Total resources for fully maxing = max level totals - current level totals
            totalPowerPointsNeeded =
                (maxLevel.totalPowerPoints - currentLevel.totalPowerPoints) * nonMaxedBrawlers
            totalCoinsNeeded = (maxLevel.totalCoins - currentLevel.totalCoins) * nonMaxedBrawlers
        }

        _state.update {
            it.copy(
                neededPowerPoints = totalPowerPointsNeeded,
                neededCoins = totalCoinsNeeded,
                maxedBrawlers = maxedBrawlers,
                totalBrawlers = brawlerCount
            )
        }
    }

    /**
     * Update projected resources based on selected timeframe
     */
    fun updateFutureResources() {
        val starrDropTable = _state.value.starrDropRewards
        if (starrDropTable.isEmpty()) return

        var totalPowerPoints = basePowerPointsPerMonth
        var totalCoins = baseCoinsPerMonth
        var totalCredits = 0

        // Calculate total drops including pass drops
        val months = selectedTimeframeMonths * 30
        val dailyDrops = (dropPerDay * months)
        val passDrops = (this.passDrops * selectedTimeframeMonths)
        val totalDrops = dailyDrops + passDrops

        _state.value.passFreeRewards?.resources?.forEach{
            if (it is Coin) {
                totalCoins += it.amount * selectedTimeframeMonths
            }
            if (it is PowerPoint) {
                totalPowerPoints += it.amount * selectedTimeframeMonths
            }
            if (it is Credit) {
                totalCredits += it.amount * selectedTimeframeMonths
            }
        }

        if(isP2WPlayer) {
            _state.value.passPremiumRewards?.resources?.forEach {
                if (it is Coin) {
                    totalCoins += it.amount * selectedTimeframeMonths
                }
                if (it is PowerPoint) {
                    totalPowerPoints += it.amount * selectedTimeframeMonths
                }
                if (it is Credit) {
                    totalCredits += it.amount * selectedTimeframeMonths
                }
            }
            _state.value.passPlusRewards?.resources?.forEach {
                if (it is Coin) {
                    totalCoins += it.amount * selectedTimeframeMonths
                }
                if (it is PowerPoint) {
                    totalPowerPoints += it.amount * selectedTimeframeMonths
                }
                if (it is Credit) {
                    totalCredits += it.amount * selectedTimeframeMonths
                }
            }
        }
        // Calculate rewards from starr drops
        for (drop in starrDropTable) {
            for (reward in drop.rewards) {
                if (reward.resource is Coin && reward.resource.name == "Coin") {
                    val coins = reward.resource.amount
                    val chance = reward.chance

                    val total = floor((totalDrops) * drop.chanceToDrop * coins * chance).toInt()
                    totalCoins += total
                }
                if (reward.resource is PowerPoint) {
                    val powerpoints = reward.resource.amount
                    val chance = reward.chance

                    val total =
                        floor((totalDrops) * drop.chanceToDrop * powerpoints * chance).toInt()
                    totalPowerPoints += total
                }

                if (reward.resource is Credit) {
                    val credits = reward.resource.amount
                    val chance = reward.chance

                    val total = floor((totalDrops) * drop.chanceToDrop * credits * chance).toInt()
                    totalCredits += total
                }
            }
        }

        // Calculate months to max out
        val neededPowerPoints = _state.value.neededPowerPoints
        val neededCoins = _state.value.neededCoins

        val monthsToMaxPowerPoints = if (totalPowerPoints > 0)
            ceil(neededPowerPoints.toDouble() / totalPowerPoints).toInt() else 0
        val monthsToMaxCoins = if (totalCoins > 0)
            ceil(neededCoins.toDouble() / totalCoins).toInt() else 0

        // Update state with new values
        val updatedState = _state.value.copy(
            projectedPowerPoints = totalPowerPoints,
            projectedCoins = totalCoins,
            projectedCredits = totalCredits,
            totalDrops = totalDrops,
            passDropsCount = passDrops,
            monthsToMaxPowerPoints = monthsToMaxPowerPoints,
            monthsToMaxCoins = monthsToMaxCoins
        )
        
        // Generate next steps advice
        val nextStepsAdvice =
            NextStepsGenerator.generateAdvice(updatedState, selectedTimeframeMonths)
        
        // Update state with projected resources and advice
        _state.update { 
            updatedState.copy(nextStepsAdvice = nextStepsAdvice)
        }

        // After updating resources, recalculate unlockable brawlers
        calculateUnlockableBrawlers()

        calculateUpgradableBrawlers()
    }

    /**
     * Calculate which brawlers can be unlocked with projected resources
     */
    private fun calculateUnlockableBrawlers() {
        val account = _state.value.account ?: return
        val brawlerTable = _state.value.brawlerTable
        val brawlersData = _state.value.brawlersData
        val projectedCredits = _state.value.projectedCredits + 180000

        if (brawlerTable.isEmpty() || brawlersData.isEmpty()) return

        // Find all brawlers that the account doesn't have
        val currentBrawlers = account.account.brawlers.map { it.name.uppercase() }
        val lockedBrawlers = brawlersData.filter { it.name.uppercase() !in currentBrawlers }
            .sortedByDescending { brawler ->
                // Find credit cost by rarity
                brawlerTable.find { it.rarity == brawler.rarity.toRarityData() }?.creditsNeeded ?: 0
            }
        Timber.tag("FutureProgressViewModel").d("Locked Brawlers: ${lockedBrawlers.map { it.name }}")

        // Calculate how many brawlers can be unlocked with projected credits
        var remainingCredits = projectedCredits
        val unlockableBrawlers = mutableListOf<UnlockableBrawler>()

        for (brawler in lockedBrawlers) {
            val cost = brawlerTable.find { it.rarity == brawler.rarity.toRarityData() }?.creditsNeeded ?: continue

            Timber.tag("FutureProgressViewModel").d("Brawler: ${brawler.name}, Cost: $cost, Remaining Credits: $remainingCredits")
            if (remainingCredits >= cost) {
                unlockableBrawlers.add(
                    UnlockableBrawler(
                        brawler = brawler,
                        cost = cost
                    )
                )
                remainingCredits -= cost
            } else {
                Timber.tag("FutureProgressViewModel").d("Not enough credits to unlock ${brawler.name}. Remaining Credits: $remainingCredits")
                continue
            }
        }

        // Set a reasonable limit to display (first batching approach)
        val limitedBrawlers = if (unlockableBrawlers.size > 20) {
            // If we have many brawlers, split them into batches
            val initialBatch = unlockableBrawlers.take(20)
            _state.update {
                it.copy(
                    hasMoreBrawlers = true,
                    totalUnlockableBrawlers = unlockableBrawlers.size,
                    allUnlockableBrawlers = unlockableBrawlers
                )
            }
            initialBatch
        } else {
            // If we have a reasonable number, show all
            _state.update {
                it.copy(
                    hasMoreBrawlers = false,
                    totalUnlockableBrawlers = unlockableBrawlers.size,
                    allUnlockableBrawlers = unlockableBrawlers
                )
            }
            unlockableBrawlers
        }

        Timber.tag("FutureProgressViewModel").d("Showing ${limitedBrawlers.size} of ${unlockableBrawlers.size} unlockable brawlers")

        _state.update {
            it.copy(
                unlockableBrawlers = limitedBrawlers,
                lockedBrawlers = lockedBrawlers
            )
        }
    }

    fun calculateUpgradableBrawlers(){
        val coins = _state.value.projectedCoins
        val powerPoints = _state.value.projectedPowerPoints

        val upgradeTable = _state.value.upgradeTable ?: return

        val accountState = _state.value.account?.account ?: return
        val account = accountState
        val maxedBrawlers = account.brawlers.filter { it.power >= 11 }
        val brawlers = account.brawlers.filter { it !in maxedBrawlers }

        val upgradableBrawlers = mutableListOf<UpgradableBrawler>()

        var remainingCoins = coins
        var remainingPowerPoints = powerPoints

        // Sort brawlers by their power level (prioritize higher level brawlers)
        val sortedBrawlers = brawlers.sortedByDescending { it.power }

        for (brawler in sortedBrawlers) {
            // Stop processing if we're out of resources
            if (remainingCoins <= 0 || remainingPowerPoints <= 0) break
            
            // Skip brawlers already at max level
            if (brawler.power >= 11) continue
            
            val initialPower = brawler.power
            var currentPower = initialPower
            
            // Upgrade until we run out of resources or reach max level
            while (currentPower < 11) {
                // Find the cost to upgrade to the next level
                val nextLevel = currentPower + 1
                val upgradeLevel = upgradeTable.levels.find { it.level == nextLevel }
                
                // If we can't find the upgrade level data, break out of the loop
                if (upgradeLevel == null) {
                    break
                }
                
                // Check if we have enough resources to upgrade
                if (remainingCoins >= upgradeLevel.coins && remainingPowerPoints >= upgradeLevel.powerPoints) {
                    // Deduct resources
                    remainingCoins -= upgradeLevel.coins
                    remainingPowerPoints -= upgradeLevel.powerPoints
                    
                    // Upgrade the brawler
                    currentPower = nextLevel
                } else {

                    // Not enough resources to upgrade further
                    break
                }
            }
            
            // Only add to upgradable list if the brawler was actually upgraded
            if (currentPower > initialPower) {
                upgradableBrawlers.add(UpgradableBrawler(brawler.id,brawler.name, initialPower, currentPower))
            }
        }

        _state.update { 
            val updatedState = it.copy(upgradableBrawlers = upgradableBrawlers)
            // Generate updated next steps advice
            val nextStepsAdvice =
                NextStepsGenerator.generateAdvice(updatedState, selectedTimeframeMonths)
            updatedState.copy(nextStepsAdvice = nextStepsAdvice)
        }
    }

    /**
     * Load more brawlers when requested by the UI
     */
    fun loadMoreBrawlers() {
        val currentState = _state.value
        if (!currentState.hasMoreBrawlers) return

        val allBrawlers = currentState.allUnlockableBrawlers
        val currentlyShowing = currentState.unlockableBrawlers.size

        // Add the next batch
        val nextBatch = allBrawlers.drop(currentlyShowing).take(20)
        val newList = currentState.unlockableBrawlers + nextBatch

        // Update state with more brawlers
        _state.update {
            it.copy(
                unlockableBrawlers = newList,
                hasMoreBrawlers = newList.size < allBrawlers.size
            )
        }

        Timber.tag("FutureProgressViewModel").d("Loaded more brawlers. Now showing ${newList.size} of ${allBrawlers.size}")
    }

    fun setTimeframe(months: Int) {
        selectedTimeframeMonths = months
        updateFutureResources()
    }

    fun setPlayerType(isP2W: Boolean) {
        isP2WPlayer = isP2W
        updateFutureResources()
    }

    /**
     * Returns example advice for UI preview and testing
     */
    fun getExampleAdvice(): String {
        return NextStepsGenerator.getExample()
    }
}

data class UnlockableBrawler(
    val brawler: BrawlerData,
    val cost: Int
)

data class UpgradableBrawler(
    val id: Long,
    val name: String,
    val from: Int,
    val to: Int
)
data class FutureProgressState(
    val account: Account? = null,
    val brawlerTable: List<BrawlerTable> = emptyList(),
    val upgradeTable: UpgradeTable? = null,
    val passFreeRewards: PassRewards? = null,
    val passPremiumRewards: PassRewards? = null,
    val passPlusRewards: PassRewards? = null,
    val starrDropRewards: List<StarrDropRewards> = emptyList(),
    val brawlersData: List<BrawlerData> = emptyList(),

    // Calculated values
    val neededPowerPoints: Int = 0,
    val neededCoins: Int = 0,
    val maxedBrawlers: Int = 0,
    val totalBrawlers: Int = 0,

    // Projected resources
    val projectedPowerPoints: Int = 0,
    val projectedCoins: Int = 0,
    val projectedCredits: Int = 0,
    val totalDrops: Int = 0,
    val passDropsCount: Int = 0,
    val monthsToMaxPowerPoints: Int = 0,
    val monthsToMaxCoins: Int = 0,

    // Unlockable brawlers
    val unlockableBrawlers: List<UnlockableBrawler> = emptyList(),
    val lockedBrawlers: List<BrawlerData> = emptyList(),

    // Upgradable brawlers
    val upgradableBrawlers: List<UpgradableBrawler> = emptyList(),

    // Lazy loading properties
    val hasMoreBrawlers: Boolean = false,
    val totalUnlockableBrawlers: Int = 0,
    val allUnlockableBrawlers: List<UnlockableBrawler> = emptyList(),
    
    // Next steps advice
    val nextStepsAdvice: String = ""
)