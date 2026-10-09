package com.stockkitty.feature.watchlist

import com.stockkitty.core.testing.data.testWatchedStock
import com.stockkitty.core.testing.repository.FakeStockRepository
import com.stockkitty.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeStockRepository
    private lateinit var viewModel: WatchlistViewModel

    @Before
    fun setUp() {
        repository = FakeStockRepository()
        viewModel = WatchlistViewModel(repository)
    }

    @Test
    fun `initial state is Loading before the stream emits`() {
        assertEquals(WatchlistUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `empty watchlist maps to Empty`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertEquals(WatchlistUiState.Empty, viewModel.uiState.value)

        collectJob.cancel()
    }

    @Test
    fun `watched stocks map to Success`() = runTest {
        val stock = testWatchedStock(symbol = "AAPL")
        repository.setWatchlist(listOf(stock))
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertEquals(WatchlistUiState.Success(listOf(stock)), viewModel.uiState.value)

        collectJob.cancel()
    }

    @Test
    fun `quotes refresh once when the screen opens`() {
        assertEquals(1, repository.refreshWatchlistCount)
    }

    @Test
    fun `failed refresh surfaces an error and keeps cached content`() = runTest {
        repository.shouldFailRefresh = true

        val failingViewModel = WatchlistViewModel(repository)

        assertNotNull(failingViewModel.errorMessage.value)
        assertEquals(false, failingViewModel.isRefreshing.value)
    }

    @Test
    fun `dismissError clears the error message`() = runTest {
        repository.shouldFailRefresh = true
        val failingViewModel = WatchlistViewModel(repository)

        failingViewModel.dismissError()

        assertNull(failingViewModel.errorMessage.value)
    }

    @Test
    fun `removeFromWatchlist removes the stock from state`() = runTest {
        repository.setWatchlist(listOf(testWatchedStock(symbol = "AAPL")))
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.removeFromWatchlist("AAPL")

        assertEquals(WatchlistUiState.Empty, viewModel.uiState.value)

        collectJob.cancel()
    }
}
