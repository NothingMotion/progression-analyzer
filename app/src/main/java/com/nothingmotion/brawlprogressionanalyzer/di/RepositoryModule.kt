package com.nothingmotion.brawlprogressionanalyzer.di

import com.nothingmotion.brawlprogressionanalyzer.data.BrawlerApi
import com.nothingmotion.brawlprogressionanalyzer.data.repository.FakeAccountRepository
import com.nothingmotion.brawlprogressionanalyzer.data.repository.FakeBrawlerRepository
import com.nothingmotion.brawlprogressionanalyzer.data.repository.FakeBrawlerTableRepository
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
    fun provideBrawlerApi(brawlerRepository: FakeBrawlerRepository): BrawlerApi {
        return BrawlerApi(brawlerRepository)
    }
} 