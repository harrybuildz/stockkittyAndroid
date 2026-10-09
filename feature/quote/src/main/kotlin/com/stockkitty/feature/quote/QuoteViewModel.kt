package com.stockkitty.feature.quote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockkitty.core.data.repository.StockRepository
import com.stockkitty.core.model.CompanyProfile
import com.stockkitty.core.model.Quote
import com.stockkitty.feature.quote.navigation.SYMBOL_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class QuoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val stockRepository: StockRepository,
) : ViewModel() {

    val symbol: String = checkNotNull(savedStateHandle[SYMBOL_ARG]) {
        "Quote screen requires a '$SYMBOL_ARG' argument"
    }

    private val profile = MutableStateFlow<CompanyProfile?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<QuoteUiState> = combine(
        stockRepository.observeQuote(symbol),
        stockRepository.observeIsWatched(symbol),
        profile,
        isRefreshing,
        errorMessage,
    ) { quote, isWatched, companyProfile, refreshing, error ->
        QuoteUiState(
            symbol = symbol,
            quote = quote,
            profile = companyProfile,
            isWatched = isWatched,
            isRefreshing = refreshing,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QuoteUiState(symbol = symbol),
    )

    init {
        refresh()
        loadProfile()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            runCatching { stockRepository.refreshQuote(symbol) }
                .onFailure { errorMessage.value = "Couldn't refresh $symbol. Showing cached data." }
            isRefreshing.value = false
        }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            if (uiState.value.isWatched) {
                stockRepository.removeFromWatchlist(symbol)
            } else {
                stockRepository.addToWatchlist(
                    symbol = symbol,
                    companyName = profile.value?.name ?: symbol,
                )
            }
        }
    }

    fun dismissError() {
        errorMessage.value = null
    }

    private fun loadProfile() {
        viewModelScope.launch {
            // Profile is nice-to-have metadata; quote data renders without it.
            runCatching { stockRepository.getCompanyProfile(symbol) }
                .onSuccess { profile.value = it }
        }
    }
}

data class QuoteUiState(
    val symbol: String,
    val quote: Quote? = null,
    val profile: CompanyProfile? = null,
    val isWatched: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)
