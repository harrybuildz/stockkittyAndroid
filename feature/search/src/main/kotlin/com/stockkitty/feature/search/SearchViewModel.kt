package com.stockkitty.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockkitty.core.data.repository.StockRepository
import com.stockkitty.core.model.StockSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Minimum characters before a lookup is issued, to avoid noisy API calls. */
private const val MIN_QUERY_LENGTH = 2
private const val SEARCH_DEBOUNCE_MS = 300L

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val stockRepository: StockRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val watchedSymbols = stockRepository.observeWatchlist()
        .map { stocks -> stocks.map { it.symbol }.toSet() }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val searchResults = _query
        .debounce(SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.trim().length < MIN_QUERY_LENGTH) {
                flowOf(SearchUiState.Idle)
            } else {
                flow<SearchUiState> {
                    val results = stockRepository.searchSymbols(query.trim())
                    emit(
                        if (results.isEmpty()) {
                            SearchUiState.NoResults
                        } else {
                            SearchUiState.Success(results.map { SearchRow(it, isWatched = false) })
                        },
                    )
                }
                    .onStart { emit(SearchUiState.Searching) }
                    .catch { emit(SearchUiState.Error) }
            }
        }

    /**
     * Search results annotated with live watchlist membership, so rows update
     * in place when the user adds a symbol.
     */
    val uiState: StateFlow<SearchUiState> = combine(searchResults, watchedSymbols) { state, watched ->
        if (state is SearchUiState.Success) {
            SearchUiState.Success(
                state.rows.map { row -> row.copy(isWatched = row.result.symbol in watched) },
            )
        } else {
            state
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState.Idle,
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun addToWatchlist(result: StockSearchResult) {
        viewModelScope.launch {
            stockRepository.addToWatchlist(result.symbol, result.description)
            // Best effort: prime the quote cache so the watchlist shows a
            // price immediately. The periodic refresh covers any failure.
            runCatching { stockRepository.refreshQuote(result.symbol) }
        }
    }
}

data class SearchRow(
    val result: StockSearchResult,
    val isWatched: Boolean,
)

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Searching : SearchUiState
    data object NoResults : SearchUiState
    data object Error : SearchUiState
    data class Success(val rows: List<SearchRow>) : SearchUiState
}
