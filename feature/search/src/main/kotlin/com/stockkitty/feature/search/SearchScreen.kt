package com.stockkitty.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockkitty.core.designsystem.component.EmptyState
import com.stockkitty.core.designsystem.component.ErrorState
import com.stockkitty.core.designsystem.component.LoadingState
import com.stockkitty.core.model.StockSearchResult

@Composable
fun SearchRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreen(
        query = query,
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onAdd = viewModel::addToWatchlist,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    query: String,
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onAdd: (StockSearchResult) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Add to watchlist") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Symbol or company name") },
                singleLine = true,
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear query")
                        }
                    }
                },
            )

            when (uiState) {
                SearchUiState.Idle -> EmptyState(
                    title = "Search for a stock",
                    message = "Try a ticker like AAPL or a company name.",
                )

                SearchUiState.Searching -> LoadingState()

                SearchUiState.NoResults -> EmptyState(
                    title = "No matches",
                    message = "Check the spelling or try a different ticker.",
                )

                SearchUiState.Error -> ErrorState(
                    message = "Search failed. Check your connection and try again.",
                )

                is SearchUiState.Success -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(uiState.rows, key = { it.result.symbol }) { row ->
                        SearchResultRow(row = row, onAdd = { onAdd(row.result) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    row: SearchRow,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.result.symbol,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = row.result.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }

        if (row.isWatched) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "${row.result.symbol} is on your watchlist",
                tint = MaterialTheme.colorScheme.primary,
            )
        } else {
            IconButton(onClick = onAdd) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add ${row.result.symbol} to watchlist",
                )
            }
        }
    }
}
