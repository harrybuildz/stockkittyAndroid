package com.stockkitty.core.testing.repository

import com.stockkitty.core.data.repository.StockRepository
import com.stockkitty.core.model.CompanyProfile
import com.stockkitty.core.model.Quote
import com.stockkitty.core.model.StockSearchResult
import com.stockkitty.core.model.WatchedStock
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory [StockRepository] test double. State is pushed in with the
 * `set*` helpers and failures are simulated via the `shouldFail*` flags.
 */
class FakeStockRepository : StockRepository {

    private val watchlist = MutableStateFlow<List<WatchedStock>>(emptyList())
    private val quotes = MutableStateFlow<Map<String, Quote>>(emptyMap())

    var searchResults: List<StockSearchResult> = emptyList()
    var companyProfile: CompanyProfile? = null
    var shouldFailRefresh = false
    var shouldFailSearch = false

    var refreshWatchlistCount = 0
        private set
    var refreshedSymbols = mutableListOf<String>()
        private set

    fun setWatchlist(stocks: List<WatchedStock>) {
        watchlist.value = stocks
    }

    fun setQuote(quote: Quote) {
        quotes.update { it + (quote.symbol to quote) }
    }

    override fun observeWatchlist(): Flow<List<WatchedStock>> = watchlist.asStateFlow()

    override fun observeQuote(symbol: String): Flow<Quote?> = quotes.map { it[symbol] }

    override fun observeIsWatched(symbol: String): Flow<Boolean> =
        watchlist.map { stocks -> stocks.any { it.symbol == symbol } }

    override suspend fun refreshWatchlistQuotes() {
        refreshWatchlistCount++
        if (shouldFailRefresh) throw IOException("Refresh failed")
    }

    override suspend fun refreshQuote(symbol: String) {
        refreshedSymbols += symbol
        if (shouldFailRefresh) throw IOException("Refresh failed")
    }

    override suspend fun addToWatchlist(symbol: String, companyName: String) {
        watchlist.update { stocks ->
            if (stocks.any { it.symbol == symbol }) {
                stocks
            } else {
                stocks + WatchedStock(
                    symbol = symbol,
                    companyName = companyName,
                    addedAtMillis = 0L,
                    quote = quotes.value[symbol],
                )
            }
        }
    }

    override suspend fun removeFromWatchlist(symbol: String) {
        watchlist.update { stocks -> stocks.filterNot { it.symbol == symbol } }
    }

    override suspend fun searchSymbols(query: String): List<StockSearchResult> {
        if (shouldFailSearch) throw IOException("Search failed")
        return searchResults
    }

    override suspend fun getCompanyProfile(symbol: String): CompanyProfile =
        companyProfile ?: throw IOException("No profile for $symbol")
}
