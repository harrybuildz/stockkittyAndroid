package com.stockkitty.feature.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.stockkitty.feature.search.SearchRoute

const val SEARCH_ROUTE = "search"

fun NavController.navigateToSearch() = navigate(SEARCH_ROUTE)

fun NavGraphBuilder.searchScreen(
    onBackClick: () -> Unit,
) {
    composable(route = SEARCH_ROUTE) {
        SearchRoute(onBackClick = onBackClick)
    }
}
