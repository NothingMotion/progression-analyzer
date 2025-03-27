package com.nothingmotion.brawlprogressionanalyzer.di

import android.content.Context
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesManagerModule {


    /**
     * Provides global access point to PreferencesManager for non-injected classes
     */
    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }


    /**
     * Static accessor for PreferencesManager when injection is not available
     * This should only be used in Application.onCreate or similar scenarios
     * where dependency injection is not yet available
     */
    @Volatile
    private var preferencesManagerInstance: PreferencesManager? = null

    /**
     * Get the singleton instance of PreferencesManager for non-injected contexts
     */
    fun getPreferencesManager(context: Context): PreferencesManager {
        return preferencesManagerInstance ?: synchronized(this) {
            preferencesManagerInstance ?: PreferencesManager(context.applicationContext).also {
                preferencesManagerInstance = it
            }
        }
    }

}