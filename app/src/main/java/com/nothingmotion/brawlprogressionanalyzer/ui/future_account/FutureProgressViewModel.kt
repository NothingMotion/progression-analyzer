package com.nothingmotion.brawlprogressionanalyzer.ui.future_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeBrawlerTableRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakePassTableRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeStarrDropTableRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeUpgradeTableRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Coin
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Credit
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PassRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PowerPoint
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.UpgradeTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.toRarityData
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerTableRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.PassRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.UpgradeTableRepository
import com.nothingmotion.brawlprogressionanalyzer.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val brawlerTableRepository: BrawlerTableRepository,
    private val upgradeTableRepository: UpgradeTableRepository,
    private val passRepository: PassRepository,
    private val starrDropRepository: FakeStarrDropTableRepository,
    private val brawlerDataRepository: BrawlerRepository,
    private val accountRepository: AccountRepository,
    private val prefManager: PreferencesManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Initialize State with all sub-states
    private val _state = MutableStateFlow(
        FutureProgressState(
            accountState = AccountState(),
            tablesState = TablesState(),
            calculatedState = CalculatedState(),
            projectedState = ProjectedState(),
            brawlersState = BrawlersState(),
            nextStepsAdvice = NextStepsAdvice()
        )
    )
    val state: StateFlow<FutureProgressState> = _state.asStateFlow()

    // Resource estimates per month
    private val basePowerPointsPerMonth = 0
    private val baseCoinsPerMonth = 0

    // Starr drop configuration
    private val dropPerDay = 3
    private val passDrops = 29

    // Default timeframe (now managed in state)
    private var selectedTimeframeMonths = 1
    private var isP2WPlayer = false

    // Number formatter
    private val numberFormat = NumberFormat.getIntegerInstance()

    init {
        loadData()
    }

    // Helper function to update specific parts of state
    private fun updateAccountState(update: (AccountState) -> AccountState) {
        _state.update { currentState ->
            currentState.copy(accountState = update(currentState.accountState ?: AccountState()))
        }
    }
    
    private fun updateTablesState(update: (TablesState) -> TablesState) {
        _state.update { currentState ->
            currentState.copy(tablesState = update(currentState.tablesState ?: TablesState()))
        }
    }
    
    private fun updateCalculatedState(update: (CalculatedState) -> CalculatedState) {
        _state.update { currentState ->
            currentState.copy(calculatedState = update(currentState.calculatedState ?: CalculatedState()))
        }
    }
    
    private fun updateProjectedState(update: (ProjectedState) -> ProjectedState) {
        _state.update { currentState ->
            currentState.copy(projectedState = update(currentState.projectedState ?: ProjectedState()))
        }
    }
    
    private fun updateBrawlersState(update: (BrawlersState) -> BrawlersState) {
        _state.update { currentState ->
            currentState.copy(brawlersState = update(currentState.brawlersState ?: BrawlersState()))
        }
    }
    
    private fun updateNextStepsAdvice(update: (NextStepsAdvice) -> NextStepsAdvice) {
        _state.update { currentState ->
            currentState.copy(nextStepsAdvice = update(currentState.nextStepsAdvice ?: NextStepsAdvice()))
        }
    }

    fun loadData() {
        viewModelScope.launch {
            val token = getAccessToken() ?: return@launch
            
            // Set loading state (would be added to state model)
            
            // Run parallel API requests with coroutines
            val upgradeTableDeferred = async(Dispatchers.IO) { 
                upgradeTableRepository.getUpgradeTable(token) 
            }
            val brawlerTableDeferred = async(Dispatchers.IO) { 
                brawlerTableRepository.getBrawlerTable(token) 
            }
            val passRewardsDeferred = async(Dispatchers.IO) {
                Triple(
                    passRepository.getPassFreeTable(token),
                    passRepository.getPassPremiumTable(token),
                    passRepository.getPassPlusTable(token)
                )
            }
            val brawlersDataDeferred = async(Dispatchers.IO) {
                brawlerDataRepository.getBrawlers()
            }
            
            // Process upgrade table
            when (val result = upgradeTableDeferred.await()) {
                is Result.Success -> {
                    updateTablesState { it.copy(upgradeTable = result.data) }
                    Timber.d("Upgrade table loaded: ${result.data}")
                }
                is Result.Error -> {
                    Timber.e("Error fetching upgrade table: ${result.error}")
                    updateTablesState { it.copy(upgradeTable = null) }
                }
                else -> { /* Loading state handled below */ }
            }
            
            // Process brawler table flow
            launch {
                brawlerTableDeferred.await().collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            updateTablesState { it.copy(brawlerTable = result.data) }
                            calculateUnlockableBrawlers()
                        }
                        is Result.Error -> {
                            Timber.e("Error fetching brawler table: ${result.error}")
                            updateTablesState { it.copy(brawlerTable = emptyList()) }
                        }
                        else -> { /* Handle loading state */ }
                    }
                }
            }
            
            // Process pass rewards
            val (freeResult, premiumResult, plusResult) = passRewardsDeferred.await()
            processPassResult(freeResult, "free")
            processPassResult(premiumResult, "premium")
            processPassResult(plusResult, "plus")
            
            // Process starr drop rewards
            launch {
                starrDropRepository.starrDropTable.collectLatest { starrDropTable ->
                    updateTablesState { it.copy(starrDropRewards = starrDropTable) }
                    updateFutureResources()
                }
            }
            
            // Process brawlers data
            launch {
                brawlersDataDeferred.await().collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            updateBrawlersState { it.copy(brawlersData = result.data) }
                            calculateUnlockableBrawlers()
                            calculateUpgradableBrawlers()
                        }
                        is Result.Error -> {
                            Timber.e("Error fetching brawlers: ${result.error}")
                            updateBrawlersState { it.copy(brawlersData = emptyList()) }
                        }
                        else -> { /* Handle loading state */ }
                    }
                }
            }
            
            // Run calculations once data is loaded
            calculateResourcesNeeded()
            updateFutureResources()
            calculateUpgradableBrawlers()
        }
    }

    private fun processPassResult(
        result: Result<PassRewards, DataError.NetworkError>,
        passType: String
    ) {
        when (result) {
            is Result.Success -> {
                Timber.d("${passType} pass rewards loaded")
                updateTablesState {
                    when(passType) {
                        "free" -> it.copy(passFreeRewards = result.data)
                        "premium" -> it.copy(passPremiumRewards = result.data)
                        "plus" -> it.copy(passPlusRewards = result.data)
                        else -> it
                    }
                }
                updateFutureResources()
            }
            is Result.Error -> {
                Timber.e("Error fetching ${passType} pass: ${result.error}")
            }
            else -> { /* Handle loading state */ }
        }
    }

    fun getAccount(accountId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                prefManager.track?.uuid?.let { uuid ->
                    tokenManager.getAccessToken(uuid.toString())?.let { token ->
                        when (val result = accountRepository.getAccount(accountId.replace("#",""), token)) {
                            is Result.Error -> {
                                Timber.e("Error fetching account: ${result.error}")
                                updateAccountState { it.copy(account = null) }
                            }

                            is Result.Loading -> {
                                Timber.d("Loading account data...")
                                // Loading state already set
                            }

                            is Result.Success -> {
                                result.data.account?.let { account ->
                                    updateAccountState { it.copy(account = result.data) }
                                    calculateResourcesNeeded()
                                    updateFutureResources()
                                    calculateUnlockableBrawlers()
                                    calculateUpgradableBrawlers()
                                }
                            }
                        }
                    } ?: run {
                        Timber.e("Error fetching account: Token is null")
                        updateAccountState { it.copy(account = null) }
                    }
                } ?: run {
                    Timber.e("Error fetching account: Track is null")
                    updateAccountState { it.copy(account = null) }
                }
            }
        }
    }

    private suspend fun getAccessToken() : String? {
        return prefManager.track?.uuid?.let {
            tokenManager.getAccessToken(it.toString())
        }
    }
    
    /**
     * Calculate power points and coins needed to max out all brawlers
     */
    private fun calculateResourcesNeeded() {
        val account = _state.value.accountState?.account ?: return
        val upgradeTable = _state.value.tablesState?.upgradeTable ?: return
        
        if(upgradeTable.levels.isNullOrEmpty()) return
        
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

        updateCalculatedState {
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
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val starrDropTable = _state.value.tablesState?.starrDropRewards ?: return@withContext
                if (starrDropTable.isEmpty()) return@withContext

                var totalPowerPoints = basePowerPointsPerMonth
                var totalCoins = baseCoinsPerMonth
                var totalCredits = 0

                // Calculate total drops including pass drops
                val months = selectedTimeframeMonths * 30
                val dailyDrops = (dropPerDay * months)
                val passDropCount = (this@FutureProgressViewModel.passDrops * selectedTimeframeMonths)
                val totalDrops = dailyDrops + passDropCount

                _state.value.tablesState?.passFreeRewards?.resources?.forEach {
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

                if (isP2WPlayer) {
                    _state.value.tablesState?.passPremiumRewards?.resources?.forEach {
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
                    _state.value.tablesState?.passPlusRewards?.resources?.forEach {
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

                            val total =
                                floor((totalDrops) * drop.chanceToDrop * coins * chance).toInt()
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

                            val total =
                                floor((totalDrops) * drop.chanceToDrop * credits * chance).toInt()
                            totalCredits += total
                        }
                    }
                }

                // Calculate months to max out
                val neededPowerPoints = _state.value.calculatedState?.neededPowerPoints ?: 0
                val neededCoins = _state.value.calculatedState?.neededCoins ?: 0

                val monthsToMaxPowerPoints = if (totalPowerPoints > 0)
                    ceil(neededPowerPoints.toDouble() / totalPowerPoints).toInt() else 0
                val monthsToMaxCoins = if (totalCoins > 0)
                    ceil(neededCoins.toDouble() / totalCoins).toInt() else 0

                // Update projected state
                updateProjectedState {
                    it.copy(
                        projectedPowerPoints = totalPowerPoints,
                        projectedCoins = totalCoins,
                        projectedCredits = totalCredits,
                        totalDrops = totalDrops,
                        passDropsCount = passDropCount,
                        monthsToMaxPowerPoints = monthsToMaxPowerPoints,
                        monthsToMaxCoins = monthsToMaxCoins
                    )
                }

                // Generate next steps advice
                val nextStepsAdvice = NextStepsGenerator.generateAdvice(_state.value, selectedTimeframeMonths)

                // Update advice text
                updateNextStepsAdvice {
                    it.copy(nextStepsAdvice = nextStepsAdvice)
                }

                // After updating resources, recalculate unlockable and upgradable brawlers
                calculateUnlockableBrawlers()
                calculateUpgradableBrawlers()
            }
        }
    }
    
    /**
     * Calculate which brawlers can be unlocked with projected resources
     */
    private fun calculateUnlockableBrawlers() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val account = _state.value.accountState?.account ?: return@withContext
                val brawlerTable = _state.value.tablesState?.brawlerTable ?: return@withContext
                val brawlersData = _state.value.brawlersState?.brawlersData ?: return@withContext
                val projectedCredits = _state.value.projectedState?.projectedCredits ?: return@withContext

                if (brawlerTable.isEmpty() || brawlersData.isEmpty()) return@withContext

                // Find all brawlers that the account doesn't have
                val currentBrawlers = account.account.brawlers.map { it.name.uppercase() }
                val lockedBrawlers = brawlersData.filter { it.name.uppercase() !in currentBrawlers }
                    .sortedByDescending { brawler ->
                        // Find credit cost by rarity
                        brawlerTable.find { it.rarity == brawler.rarity.toRarityData() }?.value ?: 0
                    }
                
                Timber.tag("FutureProgressViewModel")
                    .d("Locked Brawlers: ${lockedBrawlers.map { it.name }}")

                // Calculate how many brawlers can be unlocked with projected credits
                var remainingCredits = projectedCredits + 180000
                val unlockableBrawlers = mutableListOf<UnlockableBrawler>()

                for (brawler in lockedBrawlers) {
                    val cost =
                        brawlerTable.find { it.rarity == brawler.rarity.toRarityData() }?.value
                            ?: continue

                    Timber.tag("FutureProgressViewModel")
                        .d("Brawler: ${brawler.name}, Cost: $cost, Remaining Credits: $remainingCredits")
                    if (remainingCredits >= cost) {
                        unlockableBrawlers.add(
                            UnlockableBrawler(
                                brawler = brawler,
                                cost = cost
                            )
                        )
                        remainingCredits -= cost
                    } else {
                        Timber.tag("FutureProgressViewModel")
                            .d("Not enough credits to unlock ${brawler.name}. Remaining Credits: $remainingCredits")
                        continue
                    }
                }

                // Set a reasonable limit to display (first batching approach)
                val limitedBrawlers = if (unlockableBrawlers.size > 20) {
                    // If we have many brawlers, split them into batches
                    val initialBatch = unlockableBrawlers.take(20)
                    
                    initialBatch
                } else {
                    unlockableBrawlers
                }

                Timber.tag("FutureProgressViewModel")
                    .d("Showing ${limitedBrawlers.size} of ${unlockableBrawlers.size} unlockable brawlers")

                updateBrawlersState {
                    it.copy(
                        unlockableBrawlers = limitedBrawlers,
                        lockedBrawlers = lockedBrawlers,
                        hasMoreBrawlers = limitedBrawlers.size < unlockableBrawlers.size,
                        totalUnlockableBrawlers = unlockableBrawlers.size,
                        allUnlockableBrawlers = unlockableBrawlers
                    )
                }
            }
        }
    }
    
    fun calculateUpgradableBrawlers() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val upgradeTable = _state.value.tablesState?.upgradeTable ?: return@withContext
                if (upgradeTable.levels.isNullOrEmpty()) return@withContext
                val account = _state.value.accountState?.account?.account ?: return@withContext
                val coins = _state.value.projectedState?.projectedCoins ?: 0
                val powerPoints = _state.value.projectedState?.projectedPowerPoints ?: 0
                
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
                        upgradableBrawlers.add(
                            UpgradableBrawler(
                                brawler.id,
                                brawler.name,
                                initialPower,
                                currentPower
                            )
                        )
                    }
                }

                updateBrawlersState {
                    it.copy(upgradableBrawlers = upgradableBrawlers)
                }

                // Generate updated next steps advice
                val nextStepsAdvice = NextStepsGenerator.generateAdvice(_state.value, selectedTimeframeMonths)
                
                // Update advice text
                updateNextStepsAdvice {
                    it.copy(nextStepsAdvice = nextStepsAdvice)
                }
            }
        }
    }

    /**
     * Load more brawlers when requested by the UI
     */
    fun loadMoreBrawlers() {
        val currentState = _state.value.brawlersState ?: return
        if (!currentState.hasMoreBrawlers) return

        val allBrawlers = currentState.allUnlockableBrawlers
        val currentlyShowing = currentState.unlockableBrawlers.size

        // Add the next batch
        val nextBatch = allBrawlers.drop(currentlyShowing).take(20)
        val newList = currentState.unlockableBrawlers + nextBatch

        // Update state with more brawlers
        updateBrawlersState {
            it.copy(
                unlockableBrawlers = newList,
                hasMoreBrawlers = newList.size < allBrawlers.size
            )
        }

        Timber.tag("FutureProgressViewModel")
            .d("Loaded more brawlers. Now showing ${newList.size} of ${allBrawlers.size}")
    }

    fun setTimeframe(months: Int) {
        selectedTimeframeMonths = months
        updateFutureResources()
//        updateTablesState { it.copy() }
    }

    fun getTimeframe(): Int{
        return selectedTimeframeMonths
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

data class AccountState(
    val account: Account? = null,
)
data class TablesState(
    var brawlerTable: List<BrawlerTable> = emptyList(),
    var upgradeTable: UpgradeTable? = null,
    var passFreeRewards: PassRewards? = null,
    var passPremiumRewards: PassRewards? = null,
    var passPlusRewards: PassRewards? = null,
    var starrDropRewards: List<StarrDropRewards> = emptyList(),
)
data class CalculatedState(
    val neededPowerPoints: Int = 0,
    val neededCoins: Int = 0,
    val maxedBrawlers: Int = 0,
    val totalBrawlers: Int = 0,
)
data class ProjectedState(
    val projectedPowerPoints: Int = 0,
    val projectedCoins: Int = 0,
    val projectedCredits: Int = 0,
    val totalDrops: Int = 0,
    val passDropsCount: Int = 0,
    val monthsToMaxPowerPoints: Int = 0,
    val monthsToMaxCoins: Int = 0,

)
data class BrawlersState(
    val brawlersData: List<BrawlerData> = emptyList(),

    // Unlockable brawlers
    val unlockableBrawlers: List<UnlockableBrawler> = emptyList(),
    val lockedBrawlers: List<BrawlerData> = emptyList(),

    // Upgradable brawlers
    val upgradableBrawlers: List<UpgradableBrawler> = emptyList(),
    // Lazy loading properties
    val hasMoreBrawlers: Boolean = false,
    val totalUnlockableBrawlers: Int = 0,
    val allUnlockableBrawlers: List<UnlockableBrawler> = emptyList(),

)

data class NextStepsAdvice(
    val nextStepsAdvice: String = ""
)
data class FutureProgressState(
    val accountState: AccountState?=null,
    val tablesState: TablesState?= null,

    // Calculated State
    val calculatedState: CalculatedState?=null,


    // Projected State
    val projectedState: ProjectedState?=null,

    
    // Brawlers State
    val brawlersState: BrawlersState?=null,
    
    
    // Next steps advice
    val nextStepsAdvice: NextStepsAdvice?=null
)