package com.nothingmotion.brawlprogressionanalyzer.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.AccountEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.AccountWithRelations
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.PlayerHistoryEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.ProgressEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.ProgressType

@Dao
interface AccountDao {
    /**
     * Account operations
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>): List<Long>
    
    @Update
    suspend fun updateAccount(account: AccountEntity): Int
    
    @Delete
    suspend fun deleteAccount(account: AccountEntity): Int

    @Query("DELETE FROM account WHERE player_tag = :playerTag")
    suspend fun deleteAccountByPlayerTag(playerTag: String): Int

    @Transaction
    @Query("DELETE FROM account WHERE player_tag = :playerTag")
    suspend fun deleteAccountWithRelationsByPlayerTag(playerTag: String): Int

    @Query("SELECT * FROM account WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Int): AccountEntity?
    
    @Query("SELECT * FROM account WHERE player_tag = :playerTag LIMIT 1")
    suspend fun getAccountByPlayerTag(playerTag: String): AccountEntity?

    @Query("SELECT * FROM account")
    suspend fun getAllAccounts(): List<AccountEntity>
    
    /**
     * Player History operations
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerHistory(playerHistory: PlayerHistoryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerHistories(playerHistories: List<PlayerHistoryEntity>): List<Long>
    
    @Query("DELETE FROM player_history WHERE account_tag = :accountTag AND player_tag = :playerTag")
    suspend fun deletePlayerHistory(accountTag: String, playerTag: String): Int
    
    /**
     * Progress History operations
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressHistory(progressHistory: ProgressEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressHistories(progressHistories: List<ProgressEntity>): List<Long>
    
    @Query("DELETE FROM progress WHERE account_tag = :accountTag AND id = :progressId")
    suspend fun deleteProgressHistory(accountTag: String, progressId: Int): Int

    /**
     * Queries to get progress by type
     */
    @Query("SELECT p.* FROM progress p INNER JOIN progress ph ON p.id = ph.account_tag WHERE ph.account_tag = :accountTag AND ph.type = :type")
    suspend fun getProgressesByType(accountTag: String, type: ProgressType): List<ProgressEntity>
    
    /**
     * Complex account data operations
     */
    @Transaction
    @Query("SELECT * FROM account WHERE id = :id LIMIT 1")
    suspend fun getAccountWithRelationsById(id: Int): AccountWithRelations?
    
    @Transaction
    @Query("SELECT * FROM account WHERE player_tag = :playerTag LIMIT 1")
    suspend fun getAccountWithRelationsByPlayerTag(playerTag: String): AccountWithRelations?
    
    @Transaction
    @Query("SELECT * FROM account")
    suspend fun getAllAccountWithRelations(): List<AccountWithRelations>
    
    /**
     * Transaction to insert or update a complete account with all its relations
     */
//    @Transaction
//    suspend fun upsertCompleteAccount(
//        account: AccountEntity,
//        player: PlayerEntity,
//        currentProgress: ProgressEntity,
//        historyPlayers: List<PlayerEntity> = emptyList(),
//        previousProgresses: List<ProgressEntity> = emptyList(),
//        futureProgresses: List<ProgressEntity> = emptyList()
//    ) {
//        // Insert main entities
//        val accountId = insertAccount(account)
//        val accountIdStr = accountId.toString()
//
//        upsertPlayer(player)
//
//        // Add current progress with account tag
//        val progressWithAccountTag = currentProgress.copy(accountTag = player.tag)
//        val currentProgressId = insertProgress(progressWithAccountTag)
//
//        // Update account with current progress ID
//        val updatedAccount = account.copy(currentProgressId = currentProgressId.toString())
//        updateAccount(updatedAccount)
//
//        // Insert history players
//        if (historyPlayers.isNotEmpty()) {
//            insertPlayers(historyPlayers)
//            val playerHistories = historyPlayers.map { historyPlayer ->
//                PlayerHistoryEntity(accountIdStr, historyPlayer.tag)
//            }
//            insertPlayerHistories(playerHistories)
//        }
//
//        // Insert previous progresses
//        if (previousProgresses.isNotEmpty()) {
//            // Make sure all progresses have the correct account tag
//            val previousProgressesWithTag = previousProgresses.map { it.copy(accountTag = player.tag) }
//            val insertedIds = insertProgresses(previousProgressesWithTag)
//
//            val previousProgressHistories = insertedIds.map { progressId ->
//                ProgressHistoryEntity(accountIdStr, progressId.toString(), ProgressType.PREVIOUS)
//            }
//            insertProgressHistories(previousProgressHistories)
//        }
//
//        // Insert future progresses
//        if (futureProgresses.isNotEmpty()) {
//            // Make sure all progresses have the correct account tag
//            val futureProgressesWithTag = futureProgresses.map { it.copy(accountTag = player.tag) }
//            val insertedIds = insertProgresses(futureProgressesWithTag)
//
//            val futureProgressHistories = insertedIds.map { progressId ->
//                ProgressHistoryEntity(accountIdStr, progressId.toString(), ProgressType.FUTURE)
//            }
//            insertProgressHistories(futureProgressHistories)
//        }
//    }
//
    /**
     * Delete account and all related data
     */
    @Transaction
    suspend fun deleteAccountWithAllRelations(accountId: Int) {
        val account = getAccountById(accountId) ?: return
        
        // The database is set up with CASCADE delete,
        // so deleting the account should delete all related entries
        deleteAccount(account)
    }
}