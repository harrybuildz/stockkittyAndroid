package com.stockkitty.core.data.repository

import com.stockkitty.core.common.network.Dispatcher
import com.stockkitty.core.common.network.StockKittyDispatchers
import com.stockkitty.core.data.model.asEntity
import com.stockkitty.core.data.model.asExternalModel
import com.stockkitty.core.database.dao.QuoteDao
import com.stockkitty.core.database.dao.WatchlistDao
import com.stockkitty.core.database.entity.WatchlistEntity
import com.stockkitty.core.database.entity.asExternalModel
import com.stockkitty.core.model.CompanyProfile
import com.stockkitty.core.model.Quote
import com.stockkitty.core.model.StockSearchResult
import com.stockkitty.core.model.WatchedStock
import com.stockkitty.core.network.StockNetworkDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Offline-first [StockRepository]: Room is the single source of truth for
 * everything the UI renders, and the network is only used to refresh the
 * cache. Reads keep working with no connectivity; refreshes throw and let
 * the presentation layer decide how to surface the failure.
 */
@Singleton
class OfflineFirstStockRepository @Inject constructor(
    private val network: StockNetworkDataSource,
    private val watchlistDao: WatchlistDao,
    private val quoteDao: QuoteDao,
    @Dispatcher(StockKittyDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : StockRepository {

    override fun observeWatchlist(): Flow<List<WatchedStock>> = combine(
        watchlistDao.observeAll(),
        quoteDao.observeAll(),
    ) { watchlist, quotes ->
        val quotesBySymbol = quotes.associateBy { it.symbol }
        watchlist.map { entry ->
            WatchedStock(
                symbol = entry.symbol,
                companyName = entry.companyName,
                addedAtMillis = entry.addedAtMillis,
                quote = quotesBySymbol[entry.symbol]?.asExternalModel(),
            )
        }
    }

    override fun observeQuote(symbol: String): Flow<Quote?> =
        quoteDao.observeBySymbol(symbol).map { it?.asExternalModel() }

    override fun observeIsWatched(symbol: String): Flow<Boolean> =
        watchlistDao.observeIsWatched(symbol)

    override suspend fun refreshWatchlistQuotes() {
        val symbols = watchlistDao.getSymbols()
        if (symbols.isEmpty()) return

        // Fetch concurrently; one bad symbol should not fail the whole sync,
        // so individual failures are collected and only reported if every
        // fetch failed (likely no connectivity or an invalid API key).
        val entities = withContext(ioDispatcher) {
            coroutineScope {
                symbols.map { symbol ->
                    async { runCatching { fetchQuoteEntity(symbol) } }
                }.awaitAll()
            }
        }
        val succeeded = entities.mapNotNull { it.getOrNull() }
        if (succeeded.isEmpty()) {
            throw entities.first().exceptionOrNull() ?: IllegalStateException("Quote sync failed")
        }
        quoteDao.upsert(succeeded)
    }

    override suspend fun refreshQuote(symbol: String) {
        val entity = withContext(ioDispatcher) { fetchQuoteEntity(symbol) }
        quoteDao.upsert(listOf(entity))
    }

    override suspend fun addToWatchlist(symbol: String, companyName: String) {
        watchlistDao.insert(
            WatchlistEntity(
                symbol = symbol,
                companyName = companyName,
                addedAtMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun removeFromWatchlist(symbol: String) {
        watchlistDao.delete(symbol)
        quoteDao.delete(symbol)
    }

    override suspend fun searchSymbols(query: String): List<StockSearchResult> =
        withContext(ioDispatcher) {
            network.searchSymbols(query).result.map { it.asExternalModel() }
        }

    override suspend fun getCompanyProfile(symbol: String): CompanyProfile =
        withContext(ioDispatcher) {
            network.getCompanyProfile(symbol).asExternalModel(symbol)
        }

    private suspend fun fetchQuoteEntity(symbol: String) =
        network.getQuote(symbol).asEntity(symbol, System.currentTimeMillis())
}
