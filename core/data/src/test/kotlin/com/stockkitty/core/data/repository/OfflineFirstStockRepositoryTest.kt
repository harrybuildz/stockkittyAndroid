package com.stockkitty.core.data.repository

import com.stockkitty.core.data.testdoubles.FakeQuoteDao
import com.stockkitty.core.data.testdoubles.FakeStockNetwork
import com.stockkitty.core.data.testdoubles.FakeWatchlistDao
import com.stockkitty.core.network.model.QuoteResponse
import com.stockkitty.core.network.model.SymbolMatch
import com.stockkitty.core.network.model.SymbolSearchResponse
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstStockRepositoryTest {

    private lateinit var network: FakeStockNetwork
    private lateinit var watchlistDao: FakeWatchlistDao
    private lateinit var quoteDao: FakeQuoteDao
    private lateinit var repository: OfflineFirstStockRepository

    @Before
    fun setUp() {
        network = FakeStockNetwork()
        watchlistDao = FakeWatchlistDao()
        quoteDao = FakeQuoteDao()
    }

    private fun kotlinx.coroutines.test.TestScope.createRepository() {
        repository = OfflineFirstStockRepository(
            network = network,
            watchlistDao = watchlistDao,
            quoteDao = quoteDao,
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )
    }

    @Test
    fun `watchlist stream joins entries with cached quotes`() = runTest {
        createRepository()
        network.quotes["AAPL"] = QuoteResponse(currentPrice = 190.0, change = 1.5)

        repository.addToWatchlist("AAPL", "Apple Inc")
        repository.refreshQuote("AAPL")

        val watchlist = repository.observeWatchlist().first()
        assertEquals(1, watchlist.size)
        assertEquals("Apple Inc", watchlist.first().companyName)
        assertEquals(190.0, watchlist.first().quote?.currentPrice)
    }

    @Test
    fun `watchlist entry has null quote before first sync`() = runTest {
        createRepository()

        repository.addToWatchlist("AAPL", "Apple Inc")

        val watchlist = repository.observeWatchlist().first()
        assertNull(watchlist.first().quote)
    }

    @Test
    fun `refreshWatchlistQuotes caches a quote for every watched symbol`() = runTest {
        createRepository()
        network.quotes["AAPL"] = QuoteResponse(currentPrice = 190.0)
        network.quotes["MSFT"] = QuoteResponse(currentPrice = 410.0)
        repository.addToWatchlist("AAPL", "Apple Inc")
        repository.addToWatchlist("MSFT", "Microsoft Corp")

        repository.refreshWatchlistQuotes()

        assertEquals(190.0, repository.observeQuote("AAPL").first()?.currentPrice)
        assertEquals(410.0, repository.observeQuote("MSFT").first()?.currentPrice)
    }

    @Test
    fun `partial refresh failure keeps the quotes that succeeded`() = runTest {
        createRepository()
        network.quotes["AAPL"] = QuoteResponse(currentPrice = 190.0)
        network.failingSymbols += "BADX"
        repository.addToWatchlist("AAPL", "Apple Inc")
        repository.addToWatchlist("BADX", "Bad Symbol Corp")

        repository.refreshWatchlistQuotes()

        assertEquals(190.0, repository.observeQuote("AAPL").first()?.currentPrice)
        assertNull(repository.observeQuote("BADX").first())
    }

    @Test
    fun `refreshWatchlistQuotes throws when every fetch fails`() = runTest {
        createRepository()
        network.failingSymbols += setOf("AAPL", "MSFT")
        repository.addToWatchlist("AAPL", "Apple Inc")
        repository.addToWatchlist("MSFT", "Microsoft Corp")

        try {
            repository.refreshWatchlistQuotes()
            fail("Expected refreshWatchlistQuotes to throw")
        } catch (expected: IOException) {
            // The sync failure must propagate so the UI can report it.
        }
    }

    @Test
    fun `refreshWatchlistQuotes is a no-op for an empty watchlist`() = runTest {
        createRepository()
        network.failingSymbols += "AAPL"

        // Must not throw: there is nothing to sync.
        repository.refreshWatchlistQuotes()

        assertTrue(repository.observeWatchlist().first().isEmpty())
    }

    @Test
    fun `removeFromWatchlist also clears the cached quote`() = runTest {
        createRepository()
        network.quotes["AAPL"] = QuoteResponse(currentPrice = 190.0)
        repository.addToWatchlist("AAPL", "Apple Inc")
        repository.refreshQuote("AAPL")

        repository.removeFromWatchlist("AAPL")

        assertTrue(repository.observeWatchlist().first().isEmpty())
        assertNull(repository.observeQuote("AAPL").first())
    }

    @Test
    fun `searchSymbols maps network results to domain models`() = runTest {
        createRepository()
        network.searchResponse = SymbolSearchResponse(
            count = 1,
            result = listOf(
                SymbolMatch(symbol = "AAPL", description = "APPLE INC", type = "Common Stock"),
            ),
        )

        val results = repository.searchSymbols("apple")

        assertEquals(1, results.size)
        assertEquals("AAPL", results.first().symbol)
        assertEquals("APPLE INC", results.first().description)
    }
}
