package com.stockkitty.core.data.di

import com.stockkitty.core.data.repository.OfflineFirstStockRepository
import com.stockkitty.core.data.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    fun bindsStockRepository(impl: OfflineFirstStockRepository): StockRepository
}
