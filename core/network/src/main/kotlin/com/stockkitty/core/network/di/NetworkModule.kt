package com.stockkitty.core.network.di

import com.stockkitty.core.network.BuildConfig
import com.stockkitty.core.network.FinnhubAuthInterceptor
import com.stockkitty.core.network.StockNetworkDataSource
import com.stockkitty.core.network.retrofit.FinnhubApi
import com.stockkitty.core.network.retrofit.RetrofitStockNetwork
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun providesJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(FinnhubAuthInterceptor(BuildConfig.FINNHUB_API_KEY))
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        // BASIC avoids logging headers, which carry the API token.
                        level = HttpLoggingInterceptor.Level.BASIC
                    },
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun providesFinnhubApi(json: Json, okHttpClient: OkHttpClient): FinnhubApi = Retrofit.Builder()
        .baseUrl(BuildConfig.FINNHUB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(FinnhubApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
internal interface NetworkBindsModule {

    @Binds
    fun bindsStockNetworkDataSource(impl: RetrofitStockNetwork): StockNetworkDataSource
}
