package com.stockkitty.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stockkitty.core.model.Quote

/**
 * The latest cached quote for a symbol. Acts as the local source of truth:
 * the UI always renders from this table, and network syncs overwrite it.
 */
@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey
    @ColumnInfo(name = "symbol")
    val symbol: String,
    @ColumnInfo(name = "current_price")
    val currentPrice: Double,
    @ColumnInfo(name = "change")
    val change: Double,
    @ColumnInfo(name = "percent_change")
    val percentChange: Double,
    @ColumnInfo(name = "high")
    val high: Double,
    @ColumnInfo(name = "low")
    val low: Double,
    @ColumnInfo(name = "open")
    val open: Double,
    @ColumnInfo(name = "previous_close")
    val previousClose: Double,
    @ColumnInfo(name = "updated_at")
    val updatedAtMillis: Long,
)

fun QuoteEntity.asExternalModel(): Quote = Quote(
    symbol = symbol,
    currentPrice = currentPrice,
    change = change,
    percentChange = percentChange,
    high = high,
    low = low,
    open = open,
    previousClose = previousClose,
    updatedAtMillis = updatedAtMillis,
)
