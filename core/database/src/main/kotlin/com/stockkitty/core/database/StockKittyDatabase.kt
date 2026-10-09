package com.stockkitty.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.stockkitty.core.database.dao.QuoteDao
import com.stockkitty.core.database.dao.WatchlistDao
import com.stockkitty.core.database.entity.QuoteEntity
import com.stockkitty.core.database.entity.WatchlistEntity

@Database(
    entities = [
        WatchlistEntity::class,
        QuoteEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class StockKittyDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun quoteDao(): QuoteDao
}
