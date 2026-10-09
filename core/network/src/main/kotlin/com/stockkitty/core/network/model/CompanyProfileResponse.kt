package com.stockkitty.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Finnhub `/stock/profile2` payload. Finnhub returns an empty object for
 * unknown symbols, so every field needs a default.
 */
@Serializable
data class CompanyProfileResponse(
    @SerialName("ticker") val ticker: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("exchange") val exchange: String = "",
    @SerialName("currency") val currency: String = "",
    @SerialName("marketCapitalization") val marketCapitalization: Double = 0.0,
    @SerialName("logo") val logo: String = "",
    @SerialName("weburl") val webUrl: String = "",
)
