package com.stockkitty.feature.quote

import androidx.lifecycle.SavedStateHandle
import com.stockkitty.core.testing.data.testQuote
import com.stockkitty.core.testing.data.testWatchedStock
import com.stockkitty.core.testing.repository.FakeStockRepository
import com.stockkitty.core.testing.util.MainDispatcherRule
import com.stockkitty.feature.quote.navigation.SYMBOL_ARG
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeStockRepository
    private lateinit var viewModel: QuoteViewModel

    @Before
    fun setUp() {
        repository = FakeStockRepository()
        viewModel = QuoteViewModel(
            savedStateHandle = SavedStateHandle(mapOf(SYMBOL_ARG to "AAPL")),
            stockRepository = repository,
        )
    }

    @Test
    fun `symbol comes from navigation arguments`() {
        assertEquals("AAPL", viewModel.symbol)
    }

    @Test
    fun `opening the screen refreshes the quote`() {
        assertEquals(listOf("AAPL"), repository.refreshedSymbols)
    }

    @Test
    fun `cached quote flows into ui state`() = runTest {
        repository.setQuote(testQuote(symbol = "AAPL", currentPrice = 190.0))
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertEquals(190.0, viewModel.uiState.value.quote?.currentPrice)

        collectJob.cancel()
    }

    @Test
    fun `toggleWatchlist adds an unwatched symbol`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.toggleWatchlist()

        assertEquals(true, viewModel.uiState.value.isWatched)

        collectJob.cancel()
    }

    @Test
    fun `toggleWatchlist removes a watched symbol`() = runTest {
        repository.setWatchlist(listOf(testWatchedStock(symbol = "AAPL")))
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.toggleWatchlist()

        assertEquals(false, viewModel.uiState.value.isWatched)

        collectJob.cancel()
    }

    @Test
    fun `failed refresh surfaces an error`() = runTest {
        repository.shouldFailRefresh = true

        val failingViewModel = QuoteViewModel(
            savedStateHandle = SavedStateHandle(mapOf(SYMBOL_ARG to "AAPL")),
            stockRepository = repository,
        )
        val collectJob = launch(UnconfinedTestDispatcher()) { failingViewModel.uiState.collect() }

        assertNotNull(failingViewModel.uiState.value.errorMessage)

        collectJob.cancel()
    }
}
