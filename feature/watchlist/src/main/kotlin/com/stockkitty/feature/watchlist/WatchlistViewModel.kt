package com.stockkitty.feature.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockkitty.core.data.repository.StockRepository
import com.stockkitty.core.model.WatchedStock
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val stockRepository: StockRepository,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val uiState: StateFlow<WatchlistUiState> = stockRepository.observeWatchlist()
        .map { stocks ->
            if (stocks.isEmpty()) WatchlistUiState.Empty else WatchlistUiState.Success(stocks)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WatchlistUiState.Loading,
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            runCatching { stockRepository.refreshWatchlistQuotes() }
                .onFailure { _errorMessage.value = "Couldn't refresh quotes. Showing cached prices." }
            _isRefreshing.value = false
        }
    }

    fun removeFromWatchlist(symbol: String) {
        viewModelScope.launch {
            stockRepository.removeFromWatchlist(symbol)
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}

sealed interface WatchlistUiState {
    data object Loading : WatchlistUiState
    data object Empty : WatchlistUiState
    data class Success(val stocks: List<WatchedStock>) : WatchlistUiState
}
