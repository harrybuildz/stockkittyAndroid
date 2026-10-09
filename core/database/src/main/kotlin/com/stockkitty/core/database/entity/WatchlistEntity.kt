package com.stockkitty.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A symbol the user has added to their watchlist. Quotes are stored
 * separately in [QuoteEntity] so a sync can refresh prices without
 * touching the user's list.
 */
@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    @ColumnInfo(name = "symbol")
    val symbol: String,
    @ColumnInfo(name = "company_name")
    val companyName: String,
    @ColumnInfo(name = "added_at")
    val addedAtMillis: Long,
)
