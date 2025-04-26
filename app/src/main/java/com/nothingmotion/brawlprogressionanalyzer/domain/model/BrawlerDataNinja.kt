package com.nothingmotion.brawlprogressionanalyzer.domain.model


data class Status(
    val rarity: String
)
open class AbilityDataNinja(
    @Transient open val id: String,
    @Transient open val name: String,








    @Transient open val description: String,
)
data class GadgetDataNinja(
    override val id: String,
    override val name: String,
    override val description: String,
) : AbilityDataNinja(id, name, description) {
    val type: String = "Gadget"
}
data class StarPowerDataNinja(

    override val id: String,
    override val name: String,
    override val description: String
) : AbilityDataNinja(id, name, description) {
    val type: String = "Star Power"
}
data class BrawlerDataNinja(
    val id: String,
    val name: String,
    val description: String,
    val status: Status,
    val gadgets: List<GadgetDataNinja>,
    val starpowers: List<StarPowerDataNinja>

)