package com.nothingmotion.brawlprogressionanalyzer.data.mappers

import com.nothingmotion.brawlprogressionanalyzer.data.model.APIAccount
import com.nothingmotion.brawlprogressionanalyzer.data.model.Icon
import com.nothingmotion.brawlprogressionanalyzer.data.model.Player
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
fun Icon.toIcon() : com.nothingmotion.brawlprogressionanalyzer.domain.model.Icon {
    return com.nothingmotion.brawlprogressionanalyzer.domain.model.Icon(this.id,this.url)
}
fun Player.toPlayer(): com.nothingmotion.brawlprogressionanalyzer.domain.model.Player {
    return com.nothingmotion.brawlprogressionanalyzer.domain.model.Player(
        this.id,
        this.name,
        this.tag,
        this.trophies,
        this.highestTrophies,
        this.level,
        this.icon?.toIcon(),
        this.brawlers,
        this.createdAt
    )
}

fun APIAccount.toAccount(): Account {

    return Account(
        this.account.toPlayer(),
        listOf(),
        listOf(),
        this.currentProgress,
        listOf(),
        this.createdAt,
        this.updatedAt
    )
}