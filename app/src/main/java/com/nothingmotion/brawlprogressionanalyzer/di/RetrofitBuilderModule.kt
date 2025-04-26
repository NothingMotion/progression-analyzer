package com.nothingmotion.brawlprogressionanalyzer.di

import com.nothingmotion.brawlprogressionanalyzer.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.data.remote.BrawlNinjaApi
import com.nothingmotion.brawlprogressionanalyzer.data.remote.BrawlifyApi
import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object RetrofitBuilderModule {


    // Provide a singleton instance of Retrofit
    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        val CONNECTION_TIMEOUT = 5000L;
        val client = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(50,1,TimeUnit.MINUTES))
            .connectTimeout(CONNECTION_TIMEOUT,TimeUnit.MILLISECONDS)
            .callTimeout(CONNECTION_TIMEOUT,TimeUnit.MILLISECONDS)
            .readTimeout(CONNECTION_TIMEOUT,TimeUnit.MILLISECONDS)
            .writeTimeout(CONNECTION_TIMEOUT,TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("application-request-sender", "android")
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.PROGRESSION_ANALYZER_API)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
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

    @Singleton
    @Provides
    fun provideBrawlNinjaApi(): BrawlNinjaApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BRAWL_NINJA_CDN_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(BrawlNinjaApi::class.java)
    }
}