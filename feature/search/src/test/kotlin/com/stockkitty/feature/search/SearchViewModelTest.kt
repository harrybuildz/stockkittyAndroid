package com.stockkitty.feature.search

import com.stockkitty.core.testing.data.testSearchResult
import com.stockkitty.core.testing.repository.FakeStockRepository
import com.stockkitty.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeStockRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        repository = FakeStockRepository()
        viewModel = SearchViewModel(repository)
    }

    @Test
    fun `state is Idle until a query is entered`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        advanceUntilIdle()

        assertEquals(SearchUiState.Idle, viewModel.uiState.value)

        collectJob.cancel()
    }

    @Test
    fun `queries below the minimum length do not trigger a search`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.onQueryChange("a")
        advanceUntilIdle()

        assertEquals(SearchUiState.Idle, viewModel.uiState.value)

        collectJob.cancel()
    }

    @Test
    fun `search emits results after the debounce window`() = runTest {
        repository.searchResults = listOf(testSearchResult(symbol = "AAPL"))
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.onQueryChange("apple")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Success)
        assertEquals("AAPL", (state as SearchUiState.Success).rows.first().result.symbol)

        collectJob.cancel()
    }

    @Test
    fun `empty results map to NoResults`() = runTest {
        repository.searchResults = emptyList()
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.onQueryChange("zzzz")
        advanceUntilIdle()

        assertEquals(SearchUiState.NoResults, viewModel.uiState.value)

        collectJob.cancel()
    }

    @Test
    fun `search failure maps to Error`() = runTest {
        repository.shouldFailSearch = true
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.onQueryChange("apple")
        advanceUntilIdle()

        assertEquals(SearchUiState.Error, viewModel.uiState.value)

        collectJob.cancel()
    }

    @Test
    fun `rows reflect live watchlist membership`() = runTest {
        val result = testSearchResult(symbol = "AAPL")
        repository.searchResults = listOf(result)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.onQueryChange("apple")
        advanceUntilIdle()
        assertEquals(
            false,
            (viewModel.uiState.value as SearchUiState.Success).rows.first().isWatched,
        )

        viewModel.addToWatchlist(result)
        advanceUntilIdle()

        assertEquals(
            true,
            (viewModel.uiState.value as SearchUiState.Success).rows.first().isWatched,
        )

        collectJob.cancel()
    }

    @Test
    fun `adding a stock primes its quote cache`() = runTest {
        val result = testSearchResult(symbol = "AAPL")

        viewModel.addToWatchlist(result)
        advanceUntilIdle()

        assertEquals(listOf("AAPL"), repository.refreshedSymbols)
    }
}
