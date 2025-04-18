package com.nothingmotion.brawlprogressionanalyzer.di

import com.nothingmotion.brawlprogressionanalyzer.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.data.remote.BrawlifyApi
import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import dagger.Module
import dagger.Provides
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
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.PROGRESSION_ANALYZER_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Provide a singleton instance of ProgressionAnalyzerAPI
    @Singleton
    @Provides
    fun provideProgressionAnalyzerAPI(retrofit: Retrofit): ProgressionAnalyzerAPI {
        return retrofit.create(ProgressionAnalyzerAPI::class.java)
    }
    @Singleton
    @Provides
    fun provideBrawlifyApi() : BrawlifyApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BRAWLIFY_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(BrawlifyApi::class.java)
    }
}