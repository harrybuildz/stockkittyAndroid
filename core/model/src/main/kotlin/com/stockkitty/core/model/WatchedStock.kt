package com.stockkitty.core.model

/**
 * A stock the user is tracking, combined with its most recent cached [Quote].
 *
 * [quote] is null until the first successful sync for the symbol completes,
 * which lets the UI render a placeholder row instead of blocking the list.
 */
data class WatchedStock(
    val symbol: String,
    val companyName: String,
    val addedAtMillis: Long,
    val quote: Quote?,
)
