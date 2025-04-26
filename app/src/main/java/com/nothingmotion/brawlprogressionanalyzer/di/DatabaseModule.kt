package com.nothingmotion.brawlprogressionanalyzer.di

import android.content.Context
import androidx.room.Room
import com.nothingmotion.brawlprogressionanalyzer.data.db.ApplicationDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Provide your database instance here
    @Provides
    @Singleton
     fun provideDatabase(@ApplicationContext context: Context): ApplicationDatabase {
         return Room.databaseBuilder(
             context,
             ApplicationDatabase::class.java,
             ApplicationDatabase.DATABASE_NAME
         ).build()
     }
}