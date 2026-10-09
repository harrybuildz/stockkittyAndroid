package com.stockkitty.core.data.model

import com.stockkitty.core.database.entity.QuoteEntity
import com.stockkitty.core.model.CompanyProfile
import com.stockkitty.core.model.StockSearchResult
import com.stockkitty.core.network.model.CompanyProfileResponse
import com.stockkitty.core.network.model.QuoteResponse
import com.stockkitty.core.network.model.SymbolMatch

fun QuoteResponse.asEntity(symbol: String, updatedAtMillis: Long): QuoteEntity = QuoteEntity(
    symbol = symbol,
    currentPrice = currentPrice,
    change = change ?: 0.0,
    percentChange = percentChange ?: 0.0,
    high = high,
    low = low,
    open = open,
    previousClose = previousClose,
    updatedAtMillis = updatedAtMillis,
)

fun SymbolMatch.asExternalModel(): StockSearchResult = StockSearchResult(
    symbol = symbol,
    description = description,
    type = type,
)

fun CompanyProfileResponse.asExternalModel(symbol: String): CompanyProfile = CompanyProfile(
    symbol = symbol,
    name = name,
    exchange = exchange,
    currency = currency,
    marketCapitalization = marketCapitalization,
    logoUrl = logo.ifBlank { null },
    webUrl = webUrl.ifBlank { null },
)
