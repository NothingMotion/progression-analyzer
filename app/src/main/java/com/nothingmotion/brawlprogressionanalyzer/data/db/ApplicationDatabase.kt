package com.nothingmotion.brawlprogressionanalyzer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nothingmotion.brawlprogressionanalyzer.data.db.dao.AccountDao
import com.nothingmotion.brawlprogressionanalyzer.data.db.dao.CacheDao
import com.nothingmotion.brawlprogressionanalyzer.data.db.dao.PlayerDao
import com.nothingmotion.brawlprogressionanalyzer.data.db.dao.ProgressDao
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.AccountEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.CacheEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.PlayerEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.PlayerHistoryEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.ProgressEntity

@Database(
    entities = [
        AccountEntity::class, 
        PlayerEntity::class,
        ProgressEntity::class,
        PlayerHistoryEntity::class,
        CacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun accountDao(): AccountDao
    abstract fun progressDao(): ProgressDao
    abstract fun cacheDao(): CacheDao


    companion object {
        const val DATABASE_NAME = "brawl_progression_analyzer.db"
    }
}