package com.stockkitty.feature.quote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import coil.compose.AsyncImage
import com.stockkitty.core.designsystem.component.LoadingState
import com.stockkitty.core.designsystem.component.PriceChangeText
import com.stockkitty.core.model.Quote
import java.util.Locale

@Composable
fun QuoteRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuoteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    QuoteScreen(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onToggleWatchlist = viewModel::toggleWatchlist,
        onErrorShown = viewModel::dismissError,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuoteScreen(
    uiState: QuoteUiState,
    onRefresh: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onErrorShown: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.symbol) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleWatchlist) {
                        if (uiState.isWatched) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Remove from watchlist",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Icon(
                                Icons.Outlined.StarOutline,
                                contentDescription = "Add to watchlist",
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val quote = uiState.quote
            if (quote == null) {
                LoadingState()
            } else {
                QuoteContent(
                    uiState = uiState,
                    quote = quote,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

@Composable
private fun QuoteContent(
    uiState: QuoteUiState,
    quote: Quote,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val profile = uiState.profile
            if (profile?.logoUrl != null) {
                AsyncImage(
                    model = profile.logoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 12.dp),
                )
            }
            Column {
                Text(
                    text = profile?.name ?: uiState.symbol,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (profile != null) {
                    Text(
                        text = profile.exchange,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Column {
            Text(
                text = String.format(Locale.US, "%.2f", quote.currentPrice),
                style = MaterialTheme.typography.headlineMedium,
            )
            PriceChangeText(
                change = quote.change,
                percentChange = quote.percentChange,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatRow(label = "Open", value = quote.open)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StatRow(label = "High", value = quote.high)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StatRow(label = "Low", value = quote.low)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StatRow(label = "Previous close", value = quote.previousClose)
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = String.format(Locale.US, "%.2f", value),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
