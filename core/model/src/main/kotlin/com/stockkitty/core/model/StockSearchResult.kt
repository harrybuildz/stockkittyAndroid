package com.stockkitty.core.model

/**
 * A single match returned from a symbol lookup.
 */
data class StockSearchResult(
    val symbol: String,
    val description: String,
    val type: String,
)
