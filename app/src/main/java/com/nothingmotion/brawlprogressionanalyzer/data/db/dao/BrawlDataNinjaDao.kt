package com.nothingmotion.brawlprogressionanalyzer.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.BrawlDataNinjaEntity

@Dao
interface BrawlDataNinjaDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertBrawlDataNinja(brawlDataNinja: BrawlDataNinjaEntity)
}