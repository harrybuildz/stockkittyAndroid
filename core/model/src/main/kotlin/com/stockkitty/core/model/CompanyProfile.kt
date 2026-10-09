package com.stockkitty.core.model

/**
 * Static company metadata shown on the quote detail screen.
 */
data class CompanyProfile(
    val symbol: String,
    val name: String,
    val exchange: String,
    val currency: String,
    val marketCapitalization: Double,
    val logoUrl: String?,
    val webUrl: String?,
)
