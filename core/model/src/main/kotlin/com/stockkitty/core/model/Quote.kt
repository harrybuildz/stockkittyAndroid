package com.stockkitty.core.model

/**
 * A point-in-time price quote for a single symbol.
 */
data class Quote(
    val symbol: String,
    val currentPrice: Double,
    val change: Double,
    val percentChange: Double,
    val high: Double,
    val low: Double,
    val open: Double,
    val previousClose: Double,
    val updatedAtMillis: Long,
) {
    val isPositive: Boolean get() = change >= 0.0
}
