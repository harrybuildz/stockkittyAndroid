package com.stockkitty.feature.quote.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stockkitty.feature.quote.QuoteRoute

const val SYMBOL_ARG = "symbol"
const val QUOTE_ROUTE_BASE = "quote"
const val QUOTE_ROUTE = "$QUOTE_ROUTE_BASE/{$SYMBOL_ARG}"

fun NavController.navigateToQuote(symbol: String) = navigate("$QUOTE_ROUTE_BASE/$symbol")

fun NavGraphBuilder.quoteScreen(
    onBackClick: () -> Unit,
) {
    composable(
        route = QUOTE_ROUTE,
        arguments = listOf(
            navArgument(SYMBOL_ARG) { type = NavType.StringType },
        ),
    ) {
        QuoteRoute(onBackClick = onBackClick)
    }
}
