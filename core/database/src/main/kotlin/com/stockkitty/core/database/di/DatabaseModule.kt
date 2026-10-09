package com.stockkitty.core.database.di

import android.content.Context
import androidx.room.Room
import com.stockkitty.core.database.StockKittyDatabase
import com.stockkitty.core.database.dao.QuoteDao
import com.stockkitty.core.database.dao.WatchlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(
        @ApplicationContext context: Context,
    ): StockKittyDatabase = Room.databaseBuilder(
        context,
        StockKittyDatabase::class.java,
        "stockkitty.db",
    ).build()

    @Provides
    fun providesWatchlistDao(database: StockKittyDatabase): WatchlistDao =
        database.watchlistDao()

    @Provides
    fun providesQuoteDao(database: StockKittyDatabase): QuoteDao =
        database.quoteDao()
}
