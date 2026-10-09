package com.stockkitty.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    secondary = Slate40,
    tertiary = Sand40,
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    secondary = Slate80,
    tertiary = Sand80,
)

/**
 * Colors with market semantics (gain/loss) that sit outside the Material
 * color roles. Access via [StockKittyTheme.marketColors].
 */
data class MarketColors(
    val gain: Color,
    val loss: Color,
) {
    fun forChange(change: Double): Color = if (change >= 0.0) gain else loss
}

val LocalMarketColors = staticCompositionLocalOf {
    MarketColors(gain = MarketGain, loss = MarketLoss)
}

object StockKittyTheme {
    val marketColors: MarketColors
        @Composable get() = LocalMarketColors.current
}

@Composable
fun StockKittyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val marketColors = if (darkTheme) {
        MarketColors(gain = MarketGainDark, loss = MarketLossDark)
    } else {
        MarketColors(gain = MarketGain, loss = MarketLoss)
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalMarketColors provides marketColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = StockKittyTypography,
            content = content,
        )
    }
}
