package com.stockkitty.core.testing.data

import com.stockkitty.core.model.Quote
import com.stockkitty.core.model.StockSearchResult
import com.stockkitty.core.model.WatchedStock

fun testQuote(
    symbol: String = "AAPL",
    currentPrice: Double = 190.0,
    change: Double = 1.5,
) = Quote(
    symbol = symbol,
    currentPrice = currentPrice,
    change = change,
    percentChange = 0.8,
    high = 192.0,
    low = 188.0,
    open = 189.0,
    previousClose = 188.5,
    updatedAtMillis = 1_700_000_000_000,
)

fun testWatchedStock(
    symbol: String = "AAPL",
    companyName: String = "Apple Inc",
    quote: Quote? = testQuote(symbol = symbol),
) = WatchedStock(
    symbol = symbol,
    companyName = companyName,
    addedAtMillis = 1_700_000_000_000,
    quote = quote,
)

fun testSearchResult(
    symbol: String = "AAPL",
    description: String = "APPLE INC",
) = StockSearchResult(
    symbol = symbol,
    description = description,
    type = "Common Stock",
)
