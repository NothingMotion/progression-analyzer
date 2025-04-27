package com.nothingmotion.brawlprogressionanalyzer.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.ProgressEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.ProgressType

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgresses(progresses: List<ProgressEntity>): List<Long>

    @Update
    suspend fun updateProgress(progress: ProgressEntity): Int

    @Delete
    suspend fun deleteProgress(progress: ProgressEntity): Int

    @Query("SELECT * FROM progress WHERE id = :id LIMIT 1")
    suspend fun getProgressById(id: Int): ProgressEntity?

    @Query("SELECT * FROM progress WHERE account_tag = :accountTag")
    suspend fun getProgressesByAccountTag(accountTag: String): List<ProgressEntity>





    @Query("SELECT * FROM progress WHERE account_tag = :accountTag AND type= :progressType LIMIT 1")
    suspend fun getProgressByAccountTagAndType(accountTag: String, progressType: ProgressType= ProgressType.CURRENT): ProgressEntity?
}