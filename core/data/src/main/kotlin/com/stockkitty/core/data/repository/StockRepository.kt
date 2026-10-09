package com.stockkitty.core.data.repository

import com.stockkitty.core.model.CompanyProfile
import com.stockkitty.core.model.Quote
import com.stockkitty.core.model.StockSearchResult
import com.stockkitty.core.model.WatchedStock
import kotlinx.coroutines.flow.Flow

/**
 * Single entry point for stock data. Streams are served from the local
 * database (offline-first); the suspend functions reach the network and
 * throw on failure, leaving error presentation to the caller.
 */
interface StockRepository {

    /** The user's watchlist joined with the latest cached quote per symbol. */
    fun observeWatchlist(): Flow<List<WatchedStock>>

    /** The cached quote for [symbol], or null before the first sync. */
    fun observeQuote(symbol: String): Flow<Quote?>

    /** Whether [symbol] is currently on the watchlist. */
    fun observeIsWatched(symbol: String): Flow<Boolean>

    /** Fetches fresh quotes for every watched symbol and caches them. */
    suspend fun refreshWatchlistQuotes()

    /** Fetches and caches a fresh quote for a single [symbol]. */
    suspend fun refreshQuote(symbol: String)

    suspend fun addToWatchlist(symbol: String, companyName: String)

    suspend fun removeFromWatchlist(symbol: String)

    suspend fun searchSymbols(query: String): List<StockSearchResult>

    suspend fun getCompanyProfile(symbol: String): CompanyProfile
}
