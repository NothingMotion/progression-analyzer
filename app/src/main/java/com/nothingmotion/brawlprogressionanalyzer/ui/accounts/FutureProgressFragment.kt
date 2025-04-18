package com.nothingmotion.brawlprogressionanalyzer.ui.accounts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeBrawlerRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeBrawlerTableRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeStarrDropTableRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeUpgradeTableRepository
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentFutureProgressBinding
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerTable
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Coin
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Credit
import com.nothingmotion.brawlprogressionanalyzer.domain.model.PowerPoint
import com.nothingmotion.brawlprogressionanalyzer.domain.model.RarityData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarrDropRewards
import com.nothingmotion.brawlprogressionanalyzer.domain.model.UpgradeTable
import com.nothingmotion.brawlprogressionanalyzer.ui.components.AccordionView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

@AndroidEntryPoint
class FutureProgressFragment : Fragment() {
    private var _binding: FragmentFutureProgressBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AccountDetailViewModel by viewModels()
    private val numberFormat = NumberFormat.getIntegerInstance()
    
    @Inject lateinit var upgradeTableRepository: FakeUpgradeTableRepository
    @Inject lateinit var brawlerTableRepository: FakeBrawlerTableRepository
    @Inject lateinit var starrDropTableRepository: FakeStarrDropTableRepository
    @Inject lateinit var brawlerDataRepository: FakeBrawlerRepository

    private lateinit var account: Account
    private lateinit var upgradeTable: UpgradeTable
    private lateinit var brawlerTable: List<BrawlerTable>
    private lateinit var starrDropTable: List<StarrDropRewards>
    private lateinit var brawlersData : List<BrawlerData>

    // Resource estimates per month (based on game averages)
    private val basePowerPointsPerMonth = 0
    private val baseCoinsPerMonth = 0
    
    // Currently selected timeframe in months
    private var selectedTimeframeMonths = 1

    private val dropPerDay = 3
    private val passDrops = 29
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFutureProgressBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupTimeframeSelection()
//        setupReadMoreAccordion()

        // Get account ID from arguments and load account data
        arguments?.getString("accountId")?.let { accountId ->
            observeAccount(accountId)
        }
        
        // Observe the brawler table
        observeBrawlerTable()
        // Observe the upgrade table
        observeUpgradeTable()
        // Observe the starr drop table

        observeStarrDropTable()
        observeBrawlers()
    }

    private fun observeBrawlers() {
        lifecycleScope.launch {
            brawlerDataRepository.brawlers.collectLatest { brawlers ->
                // Handle the list of brawlers here

                brawlersData = brawlers
            }
        }
    }


    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupTimeframeSelection() {
        binding.timeframeChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds[0]
                selectedTimeframeMonths = when (chipId) {
                    R.id.month_1_chip -> 1
                    R.id.month_3_chip -> 3
                    R.id.month_6_chip -> 6
                    R.id.month_12_chip -> 12
                    else -> 1
                }
                setUpStarrDropRewards()
                // Update future resources based on selected timeframe
                if (::account.isInitialized) {
                    updateFutureResources()
                }
            }
        }
    }






    private fun setupPlayertypeSelection(){
        binding.playerTypeChipGroup.setOnCheckedStateChangeListener{ group,checkedIds ->
            if(checkedIds.isNotEmpty()){
                val chipId = checkedIds[0]
                val selectedType = when(chipId){
                    R.id.casual_chip -> TODO()
                    R.id.competitive_chip -> TODO()

                    else ->TODO()
                }
            }
        }
    }
    private fun observeAccount(accountId: String) {
        // Load account details
        viewModel.getAccount(accountId)
        
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.account.collectLatest { account ->
                    account?.let {
                        this@FutureProgressFragment.account = it
                        if (::upgradeTable.isInitialized) {
                            updateUI()
                        }
                    }
                }
            }
        }
    }
    
    private fun observeUpgradeTable() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                upgradeTableRepository.upgradeTable.collectLatest { upgradeTable ->
                    this@FutureProgressFragment.upgradeTable = upgradeTable
                    if (::upgradeTable.isInitialized && ::brawlerTable.isInitialized) {
                        updateUI()
                    }
                }
            }
        }
    }

    private fun observeBrawlerTable(){
        lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                brawlerTableRepository.brawlerTable.collectLatest {brawlerTable->
                    this@FutureProgressFragment.brawlerTable = brawlerTable;
                    if(::brawlerTable.isInitialized && ::upgradeTable.isInitialized) {
                        updateUI()
                    }
                }
            }
        }
    }
    private fun observeStarrDropTable() {
        lifecycleScope.launch {
            starrDropTableRepository.starrDropTable.collectLatest {
                starrDropTable = it
                setUpStarrDropRewards()
            }
        }
    }

    private fun updateUI() {
        // Calculate resources needed
        val resourcesNeeded = calculateResourcesNeeded()

        val totalBrawlers = account.account.brawlers.size
        val maxedBrawlers = account.account.brawlers.count { it.power >= 11 }

        // Update UI with calculated values
        with(binding) {
            // Total Power Points needed
            powerpointsValue.text = numberFormat.format(resourcesNeeded.first)
            
            // Total Coins needed
            coinsValue.text = numberFormat.format(resourcesNeeded.second)


            // Total Credits
            creditsValue.text = numberFormat.format(1000000000)

            // Currently maxed out brawlers
            currentlyMaxedOut.text = "($maxedBrawlers/$totalBrawlers)"


            // Populate upgrade table
            populateUpgradeTable()
            // Populate brawler table
            populateBrawlerTable()
            
            // Update future resources based on current timeframe selection
            updateFutureResources()
        }
    }

    private fun setUpStarrDropRewards(){
        if(starrDropTable.isEmpty()) return
        
        // Find the parent container where all accordions will be added
        val parentContainer = binding.starrdropsContainer
        
        // Clear any existing views
        parentContainer.removeAllViews()
        val months = selectedTimeframeMonths * 30
        val dailyDrops = (dropPerDay * months)
        val passDrops = (passDrops * selectedTimeframeMonths)
        val totalDrops = dailyDrops + passDrops
        binding.totalDropsText.text = "Total: ${totalDrops} ($passDrops Pass Drops)"

        // Create and add an accordion view for each rarity type
        for(table in starrDropTable){
            val rarity = table.rarity
            
            // Create a new accordion view for this rarity
            val accordionView = createAccordionForRarity(rarity, table.chanceToDrop)
            
            // Create a grid layout for this rarity
            val gridLayout = GridLayout(requireContext()).apply {
                id = View.generateViewId()
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                // Set columns (adjust as needed for your grid)
                columnCount = 3
                // Add padding
                setPadding(8, 8, 8, 8)
            }
            
            // Add resource items to the grid
            for(reward in table.rewards){
                Log.d("StarrDrop", "Reward: ${reward.resource.name}, Chance: ${reward.chance}")
                val card = LayoutInflater.from(requireContext()).inflate(
                    R.layout.item_starrdrop_resource, 
                    gridLayout, 
                    false
                ) as MaterialCardView
                
                val image = card.findViewById<ImageView>(R.id.resource_image)
                val name = card.findViewById<TextView>(R.id.resource_name)
                val amount = card.findViewById<TextView>(R.id.resource_amount)
                val chance = card.findViewById<TextView>(R.id.resource_chance)

                name.text = reward.resource.name
                amount.text = reward.resource.amount.toString()
                chance.text = "${(reward.chance * 100).toInt()}%"
                
                // Set card margins
                val layoutParams = GridLayout.LayoutParams().apply {
                    setMargins(8, 8, 8, 8)
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                card.layoutParams = layoutParams
                
                // Add card to grid
                gridLayout.addView(card)
            }
            
            // Create a container for the grid layout
            val gridContainer = LinearLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                setPadding(8, 8, 8, 8)
            }
            
            // Add the grid to the container
            gridContainer.addView(gridLayout)
            
            // Add the container to the accordion
            accordionView.addContent(gridContainer)
            
            // Add accordion to parent container
            parentContainer.addView(accordionView)
        }
    }
    
    /**
     * Create a new accordion view for a specific rarity
     */
    private fun createAccordionForRarity(rarity: RarityData, chanceToDrop: Float): AccordionView {
        val months = selectedTimeframeMonths * 30
        val passDrops = (passDrops * selectedTimeframeMonths)
        val dailyDrops = (dropPerDay * months)
        val totalDrops = (dailyDrops) +  (passDrops)
        val amount = floor((dailyDrops + passDrops) * chanceToDrop).toInt()

        val title = "x$amount ${rarity.name} (${(chanceToDrop * 100).toInt()}% drop chance)"
        
        return AccordionView(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
                setHeaderPadding(12)
            }
            setTitle(title)
            
            // Set background color based on rarity
            val backgroundColor = when(rarity) {
                RarityData.COMMON -> R.color.table_row_even
                RarityData.RARE -> R.color.rare_color
                RarityData.SUPER_RARE -> R.color.super_rare_color
                RarityData.EPIC -> R.color.epic_color
                RarityData.MYTHIC -> R.color.mythic_color
                RarityData.LEGENDARY -> R.color.legendary_color
            }
            
            // Try to set background color with fallback
            try {
                val color = ContextCompat.getColor(context, backgroundColor)
                setCardBackgroundColor(color)
            } catch(e: Exception) {
                // Fallback to a default color if resource not found
                Log.e("FutureProgressFragment", "Color resource not found: $backgroundColor", e)
            }
            
            // Set some additional properties to ensure proper display

            setAccordionCornerRadius(8f)
            setAccordionMargins(8, 4)

        }
    }
    
    /**
     * Calculate power points and coins needed to max out all brawlers
     * Returns a Pair<PowerPoints, Coins>
     */
    private fun calculateResourcesNeeded(): Pair<Int, Int> {
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
            totalPowerPointsNeeded = (maxLevel.totalPowerPoints - currentLevel.totalPowerPoints) * nonMaxedBrawlers
            totalCoinsNeeded = (maxLevel.totalCoins - currentLevel.totalCoins) * nonMaxedBrawlers
        }
        
        return Pair(totalPowerPointsNeeded, totalCoinsNeeded)
    }
    
    private fun populateUpgradeTable() {
        val tableLayout = binding.upgradeTable
        tableLayout.removeAllViews()
        
        // Add header row
        val headerRow = LayoutInflater.from(requireContext()).inflate(
            R.layout.item_upgrade_table_header, tableLayout, false
        ) as TableRow
        tableLayout.addView(headerRow)
        
        // Add data rows for each level
        for (level in upgradeTable.levels) {
            val row = LayoutInflater.from(requireContext()).inflate(
                R.layout.item_upgrade_table_row, tableLayout, false
            ) as TableRow
            
            // Alternate row colors for better readability
            if (level.level % 2 == 0) {
                row.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.table_row_even))
            }
            
            // Set values to TextViews in the row
            row.findViewById<TextView>(R.id.level_text).text = level.level.toString()
            row.findViewById<TextView>(R.id.powerpoints_text).text = numberFormat.format(level.powerPoints)
            row.findViewById<TextView>(R.id.coins_text).text = numberFormat.format(level.coins)
            row.findViewById<TextView>(R.id.total_powerpoints_text).text = numberFormat.format(level.totalPowerPoints)
            row.findViewById<TextView>(R.id.total_coins_text).text = numberFormat.format(level.totalCoins)
            
            tableLayout.addView(row)
        }
    }
    private fun populateBrawlerTable(){
        val tableLayout = binding.creditsTable
        tableLayout.removeAllViews()

        val headerRow = LayoutInflater.from(requireContext()).inflate(R.layout.item_brawler_table_header,tableLayout,false) as TableRow

        tableLayout.addView(headerRow)
        var i: Int= 0;
        for(table in brawlerTable){
            val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_brawler_table_row,tableLayout,false) as TableRow
            i ++
            if(i % 2 == 0){
                row.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.table_row_even))
            }

            row.findViewById<TextView>(R.id.rarity_text ).text = table.rarity.name
            row.findViewById<TextView>(R.id.credits_text).text = table.creditsNeeded.toString()

            tableLayout.addView(row)
        }

    }
    private fun updateFutureResources() {
        val resourcesNeeded = calculateResourcesNeeded()
        val powerPointsNeeded = resourcesNeeded.first
        val coinsNeeded = resourcesNeeded.second
        
        // Calculate projected resources based on timeframe
        val powerPointsPerMonth = basePowerPointsPerMonth
        val coinsPerMonth = baseCoinsPerMonth
        
        var totalPowerPoints : Int = powerPointsPerMonth
        var totalCoins: Int = coinsPerMonth
        var totalCredits : Int = 0


        val months = selectedTimeframeMonths * 30
        val dailyDrops = (dropPerDay * months)
        val passDrops = (passDrops * selectedTimeframeMonths)
        val totalDrops = (dailyDrops) +  (passDrops)
        // Calculate total drops including pass drops
        for(drop in starrDropTable){
            for(reward in drop.rewards){
                if(reward.resource is Coin && reward.resource.name == "Coin"){
                    val coins = reward.resource.amount
                    val chance = reward.chance

                    val total = floor((totalDrops) * drop.chanceToDrop * coins * chance).toInt()
                    totalCoins += total;
                }
                if(reward.resource is PowerPoint){
                    val powerpoints = reward.resource.amount
                    val chance = reward.chance

                    val total = floor((totalDrops) * drop.chanceToDrop * powerpoints * chance).toInt()
                    totalPowerPoints += total;
                }

                if(reward.resource is Credit){
                    val credits = reward.resource.amount
                    val chance = reward.chance

                    val total = floor((totalDrops) * drop.chanceToDrop * credits * chance).toInt()
                    totalCredits += total;
                }
            }
        }
        // Update UI with calculated values
        with(binding) {
            // Power points per month
            powerpointsPerMonthValue.text = numberFormat.format(totalPowerPoints)
            
            // Coins per month
            coinsPerMonthValue.text = numberFormat.format(totalCoins)


            // Credits per month
            creditsPerMonthValue.text = numberFormat.format(totalCredits)

            // Months to max out calculations
            val monthsToMaxPowerPoints = ceil(powerPointsNeeded.toDouble() / powerPointsPerMonth).toInt()
            val monthsToMaxCoins = ceil(coinsNeeded.toDouble() / coinsPerMonth).toInt()
            
            // Show estimated time to max out
            powerpointsMonthsToMax.text = "It will take approximately $monthsToMaxPowerPoints months to collect enough Power Points"
            coinsMonthsToMax.text = "It will take approximately $monthsToMaxCoins months to collect enough Coins"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 