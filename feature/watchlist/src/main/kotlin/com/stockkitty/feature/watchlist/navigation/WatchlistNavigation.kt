package com.stockkitty.feature.watchlist.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.stockkitty.feature.watchlist.WatchlistRoute

const val WATCHLIST_ROUTE = "watchlist"

fun NavGraphBuilder.watchlistScreen(
    onStockClick: (String) -> Unit,
    onAddClick: () -> Unit,
) {
    composable(route = WATCHLIST_ROUTE) {
        WatchlistRoute(
            onStockClick = onStockClick,
            onAddClick = onAddClick,
        )
    }
}
