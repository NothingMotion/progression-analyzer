package com.nothingmotion.brawlprogressionanalyzer.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.AccountData
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.AccountEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.BrawlerEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.PlayerEntity







@Dao
interface BrawlerDao {
    @Upsert
    suspend fun insertBrawlers(brawler:BrawlerEntity)

    @Upsert
    suspend fun insertBrawlers(brawlers:List<BrawlerEntity>)
}

@Dao
interface PlayerDao {
    @Upsert
    suspend fun insertPlayer(player: PlayerEntity)

    @Insert
    suspend fun insertPlayers(players: List<PlayerEntity>)
}

@Dao
interface AccountDao {
    @Insert
    suspend fun insertAccount(account: AccountEntity)

    @Insert
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Query("SELECT * FROM account WHERE account_tag = :tag LIMIT 1")
    suspend fun getAccountByTag(tag: String): AccountEntity?

    @Transaction
    @Query("SELECT * FROM account WHERE account_tag = :tag LIMIT 1")
    suspend fun getAccountDataByTag(tag: String): AccountData?
    @Query("SELECT * FROM account")
    suspend fun getAllAccounts(): List<AccountEntity>?

    @Transaction
    @Query("SELECT * FROM account")
    suspend fun getAllAccountsData(): List<AccountData>?
}