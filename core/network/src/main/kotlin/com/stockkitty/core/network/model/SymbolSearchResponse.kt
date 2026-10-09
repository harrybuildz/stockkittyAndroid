package com.stockkitty.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Finnhub `/search` payload.
 */
@Serializable
data class SymbolSearchResponse(
    @SerialName("count") val count: Int = 0,
    @SerialName("result") val result: List<SymbolMatch> = emptyList(),
)

@Serializable
data class SymbolMatch(
    @SerialName("symbol") val symbol: String,
    @SerialName("description") val description: String = "",
    @SerialName("type") val type: String = "",
)
