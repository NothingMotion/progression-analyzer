package com.nothingmotion.brawlprogressionanalyzer.data.remote.mappers

import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIAccount
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIHistory
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Icon

fun Icon.toIcon() : com.nothingmotion.brawlprogressionanalyzer.domain.model.Icon {
    return com.nothingmotion.brawlprogressionanalyzer.domain.model.Icon(this.id,"")
}
fun APIHistory.toPlayer(): com.nothingmotion.brawlprogressionanalyzer.domain.model.Player {
    return com.nothingmotion.brawlprogressionanalyzer.domain.model.Player(
        this.name,
        "",
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
        this.account,
        listOf(),
        this.previousProgresses,
        this.currentProgress,
        this.futureProgresses,
        this.createdAt,
        this.updatedAt
    )
}