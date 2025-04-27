package com.nothingmotion.brawlprogressionanalyzer.data.db.dao

import androidx.room.Dao

@Dao
interface CacheDao {
    /**
     * Cache operations
     */
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: com.nothingmotion.brawlprogressionanalyzer.data.db.models.CacheEntity): Long

    @androidx.room.Query("SELECT * FROM cache WHERE player_tag = :playerTag LIMIT 1")
    suspend fun getCacheByPlayerTag(playerTag: String): com.nothingmotion.brawlprogressionanalyzer.data.db.models.CacheEntity?

    @androidx.room.Query("SELECT * FROM cache")
    suspend fun getAllCaches(): List<com.nothingmotion.brawlprogressionanalyzer.data.db.models.CacheEntity>
    @androidx.room.Query("DELETE FROM cache WHERE player_tag = :playerTag")
    suspend fun deleteCache(playerTag: String): Int
}