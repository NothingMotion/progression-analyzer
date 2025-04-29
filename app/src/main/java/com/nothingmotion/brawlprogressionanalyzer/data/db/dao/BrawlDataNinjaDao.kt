package com.nothingmotion.brawlprogressionanalyzer.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.BrawlDataNinjaEntity

@Dao
interface BrawlDataNinjaDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertBrawlDataNinja(brawlDataNinja: BrawlDataNinjaEntity)


    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertBrawlDataNinjaList(brawlDataNinjaList: List<BrawlDataNinjaEntity>)


    @Query("SELECT * FROM brawl_data_ninja WHERE name LIKE :name LIMIT 1")
    suspend fun getBrawlDataNinja(name: String): BrawlDataNinjaEntity?


    @Query("SELECT * FROM brawl_data_ninja")
    suspend fun getAllBrawlDataNinja(): List<BrawlDataNinjaEntity>
}