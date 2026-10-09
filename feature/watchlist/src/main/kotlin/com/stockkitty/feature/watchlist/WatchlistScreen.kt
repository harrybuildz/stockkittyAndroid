package com.stockkitty.feature.watchlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockkitty.core.designsystem.component.EmptyState
import com.stockkitty.core.designsystem.component.LoadingState
import com.stockkitty.core.designsystem.component.PriceChangeText
import com.stockkitty.core.model.WatchedStock
import java.util.Locale

@Composable
fun WatchlistRoute(
    onStockClick: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchlistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    WatchlistScreen(
        uiState = uiState,
        isRefreshing = isRefreshing,
        errorMessage = errorMessage,
        onRefresh = viewModel::refresh,
        onStockClick = onStockClick,
        onAddClick = onAddClick,
        onRemove = viewModel::removeFromWatchlist,
        onErrorShown = viewModel::dismissError,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WatchlistScreen(
    uiState: WatchlistUiState,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onStockClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onRemove: (String) -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Watchlist") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add stock")
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState) {
                WatchlistUiState.Loading -> LoadingState()

                WatchlistUiState.Empty -> EmptyState(
                    title = "Your watchlist is empty",
                    message = "Tap + to search for a stock and start tracking it.",
                )

                is WatchlistUiState.Success -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                ) {
                    items(uiState.stocks, key = { it.symbol }) { stock ->
                        WatchedStockCard(
                            stock = stock,
                            onClick = { onStockClick(stock.symbol) },
                            onRemove = { onRemove(stock.symbol) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchedStockCard(
    stock: WatchedStock,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stock.companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            val quote = stock.quote
            if (quote != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "%.2f", quote.currentPrice),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    PriceChangeText(
                        change = quote.change,
                        percentChange = quote.percentChange,
                    )
                }
            } else {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Remove ${stock.symbol} from watchlist",
                )
            }
        }
    }
}
