package com.nothingmotion.brawlprogressionanalyzer.di

import com.nothingmotion.brawlprogressionanalyzer.data.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.data.repository.AccountRepositoryImpl
import com.nothingmotion.brawlprogressionanalyzer.data.repository.fake.FakeAccountRepository
import com.nothingmotion.brawlprogressionanalyzer.data.repository.fake.FakeBrawlerRepository
import com.nothingmotion.brawlprogressionanalyzer.data.repository.fake.FakeBrawlerTableRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideFakeAccountRepository(): FakeAccountRepository {
        return FakeAccountRepository()
    }
    @Provides
    @Singleton
    fun provideFakeBrawlerTableRepository() : FakeBrawlerTableRepository {
        return FakeBrawlerTableRepository()
    }
    
    @Provides
    @Singleton
    fun provideFakeBrawlerRepository(): FakeBrawlerRepository {
        return FakeBrawlerRepository()
    }

    @Provides
    @Singleton
    fun provideAccountRepository (progressionAnalyzerAPI: ProgressionAnalyzerAPI) : AccountRepository {
        return AccountRepositoryImpl(progressionAnalyzerAPI)
    }
} 