package com.stockkitty.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.stockkitty.feature.quote.navigation.navigateToQuote
import com.stockkitty.feature.quote.navigation.quoteScreen
import com.stockkitty.feature.search.navigation.navigateToSearch
import com.stockkitty.feature.search.navigation.searchScreen
import com.stockkitty.feature.watchlist.navigation.WATCHLIST_ROUTE
import com.stockkitty.feature.watchlist.navigation.watchlistScreen

@Composable
fun StockKittyNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = WATCHLIST_ROUTE,
        modifier = modifier,
    ) {
        watchlistScreen(
            onStockClick = navController::navigateToQuote,
            onAddClick = navController::navigateToSearch,
        )
        searchScreen(
            onBackClick = navController::popBackStack,
        )
        quoteScreen(
            onBackClick = navController::popBackStack,
        )
    }
}
