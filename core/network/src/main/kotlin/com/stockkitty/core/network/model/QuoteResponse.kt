package com.stockkitty.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Finnhub `/quote` payload. Field names mirror Finnhub's terse schema,
 * renamed here for readability.
 */
@Serializable
data class QuoteResponse(
    @SerialName("c") val currentPrice: Double = 0.0,
    @SerialName("d") val change: Double? = null,
    @SerialName("dp") val percentChange: Double? = null,
    @SerialName("h") val high: Double = 0.0,
    @SerialName("l") val low: Double = 0.0,
    @SerialName("o") val open: Double = 0.0,
    @SerialName("pc") val previousClose: Double = 0.0,
    @SerialName("t") val timestampSeconds: Long = 0L,
)
