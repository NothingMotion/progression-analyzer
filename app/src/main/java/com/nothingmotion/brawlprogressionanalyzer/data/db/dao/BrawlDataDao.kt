package com.nothingmotion.brawlprogressionanalyzer.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.BrawlDataEntity

@Dao
interface BrawlDataDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertBrawlerData(brawlerData: BrawlDataEntity)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertBrawlerDataList(brawlerDataList: List<BrawlDataEntity>)
    @Query("SELECT * FROM brawl_data WHERE id = :id")
    suspend fun getBrawlerDataById(id: Long): BrawlDataEntity?

    @Query("SELECT * FROM brawl_data")
    suspend fun getAllBrawlerData(): List<BrawlDataEntity>
}