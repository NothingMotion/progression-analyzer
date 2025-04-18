package com.nothingmotion.brawlprogressionanalyzer.di

import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.AccountRepositoryImpl
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.BrawlerRepositoryImpl
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.BrawlerTableRepositoryImpl
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.NotMotRepositoryImpl
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.PassRepositoryImpl
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.StarrDropRepositoryImpl
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.TokenRepositoryImpl
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.UpgradeTableRepositoryImpl
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeAccountRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeBrawlerRepository
import com.nothingmotion.brawlprogressionanalyzer.data.remote.repository.fake.FakeBrawlerTableRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerTableRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.NotMotRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.PassRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.StarrDropRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.TokenRepository
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.UpgradeTableRepository
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

    @Binds
    @Singleton
    abstract fun bindBrawlerRepository(brawlerRepositoryImpl: BrawlerRepositoryImpl) : BrawlerRepository

    @Binds
    @Singleton
    abstract fun bindBrawlerTableRepository(brawlerTableRepositoryImpl: BrawlerTableRepositoryImpl): BrawlerTableRepository

    @Binds
    @Singleton
    abstract fun bindUpgradeTableRepository(upgradeTableRepositoryImpl: UpgradeTableRepositoryImpl): UpgradeTableRepository

    @Binds
    @Singleton
    abstract fun bindStarrDropRepository(starrDropRepositoryImpl: StarrDropRepositoryImpl) : StarrDropRepository

    @Binds
    @Singleton
    abstract fun bindPassRepository(passRepositoryImpl: PassRepositoryImpl) : PassRepository

    @Binds
    @Singleton
    abstract fun bindTokenRepository(tokenRepositoryImpl: TokenRepositoryImpl): TokenRepository

    @Binds
    @Singleton
    abstract fun bindNotMotRepository(notMotRepositoryImpl: NotMotRepositoryImpl): NotMotRepository
}