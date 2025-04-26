package com.nothingmotion.brawlprogressionanalyzer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nothingmotion.brawlprogressionanalyzer.data.db.dao.AccountDao
import com.nothingmotion.brawlprogressionanalyzer.data.db.dao.BrawlerDao
import com.nothingmotion.brawlprogressionanalyzer.data.db.dao.PlayerDao
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.AccountEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.BrawlerEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.PlayerEntity

@Database(
    entities = [AccountEntity::class, PlayerEntity::class,BrawlerEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ApplicationDatabase : RoomDatabase(){

    abstract fun playerDao(): PlayerDao

    abstract fun accountDao(): AccountDao

    abstract fun brawlerDao(): BrawlerDao
    companion object {
        const val DATABASE_NAME = "brawl_progression_analyzer.db"
    }
}