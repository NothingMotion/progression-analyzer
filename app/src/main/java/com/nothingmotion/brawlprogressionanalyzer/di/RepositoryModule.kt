package com.nothingmotion.brawlprogressionanalyzer.di

import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.AccountRepositoryImpl
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeAccountRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeBrawlerRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeBrawlerTableRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import dagger.Binds
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
}
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModuleBinding{

    @Binds
    @Singleton
    abstract fun bindAccountRepository (accountRepositoryImpl: AccountRepositoryImpl) : AccountRepository

}