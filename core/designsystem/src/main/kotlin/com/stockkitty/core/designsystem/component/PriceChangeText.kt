package com.stockkitty.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.stockkitty.core.designsystem.theme.StockKittyTheme
import java.util.Locale

/**
 * Renders a price change like "+1.24 (+0.85%)" tinted with the market
 * gain/loss color for the current theme.
 */
@Composable
fun PriceChangeText(
    change: Double,
    percentChange: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val sign = if (change >= 0) "+" else ""
    Text(
        text = String.format(Locale.US, "%s%.2f (%s%.2f%%)", sign, change, sign, percentChange),
        color = StockKittyTheme.marketColors.forChange(change),
        style = style,
        modifier = modifier,
    )
}
