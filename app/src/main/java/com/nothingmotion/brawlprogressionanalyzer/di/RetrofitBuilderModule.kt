package com.nothingmotion.brawlprogressionanalyzer.di

import com.nothingmotion.brawlprogressionanalyzer.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitBuilderModule {


    // Provide a singleton instance of Retrofit
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.PROGRESSION_ANALYZER_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Provide a singleton instance of ProgressionAnalyzerAPI
    @Singleton
    fun provideProgressionAnalyzerAPI(retrofit: Retrofit): ProgressionAnalyzerAPI {
        return retrofit.create(ProgressionAnalyzerAPI::class.java)
    }
}