package com.nothingmotion.brawlprogressionanalyzer.crashlytics.di

import android.content.Context
import com.nothingmotion.brawlprogressionanalyzer.BrawlAnalyzerApp
import com.nothingmotion.brawlprogressionanalyzer.crashlytics.common.CrashLytics
import com.nothingmotion.brawlprogressionanalyzer.crashlytics.common.CrashLyticsWorkerFactory
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.NotMotRepository
import com.nothingmotion.brawlprogressionanalyzer.util.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object CrashLyticsModule {

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context:Context): Context {

        return context
    };

    @Singleton
    @Provides
    fun provideBaseApplicationContext(@ApplicationContext context: Context):BrawlAnalyzerApp{

        return context as BrawlAnalyzerApp
    }
    @Singleton
    @Provides
    fun provideExceptionHandler(@ApplicationContext context: Context,prefManager: PreferencesManager): CrashLytics.ExceptionHandler {
        return CrashLytics.ExceptionHandler(context,
            Thread.getDefaultUncaughtExceptionHandler(),prefManager)
    }


    @Singleton
    @Provides
    fun provideCrashLyticsWorkerFactory(repository: NotMotRepository,tokenManager: TokenManager,prefManager: PreferencesManager): CrashLyticsWorkerFactory{
        return CrashLyticsWorkerFactory(repository,tokenManager,prefManager)
    }

}
