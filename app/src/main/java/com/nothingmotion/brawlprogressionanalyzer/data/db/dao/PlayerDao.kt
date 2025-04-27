package com.nothingmotion.brawlprogressionanalyzer.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.PlayerEntity

@Dao
interface PlayerDao {
    @Upsert
    suspend fun upsertPlayer(player: PlayerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>): List<Long>

    @Update
    suspend fun updatePlayer(player: PlayerEntity): Int

    @Delete
    suspend fun deletePlayer(player: PlayerEntity): Int

    @Query("SELECT * FROM player WHERE tag = :tag LIMIT 1")
    suspend fun getPlayerByTag(tag: String): PlayerEntity?

    @Query("SELECT * FROM player")
    suspend fun getAllPlayers(): List<PlayerEntity>
}