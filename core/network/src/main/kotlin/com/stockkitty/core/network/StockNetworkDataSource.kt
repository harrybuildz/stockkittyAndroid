package com.stockkitty.core.network

import com.stockkitty.core.network.model.CompanyProfileResponse
import com.stockkitty.core.network.model.QuoteResponse
import com.stockkitty.core.network.model.SymbolSearchResponse

/**
 * Abstraction over the remote stock API so the data layer can be tested
 * against a fake instead of a live Retrofit stack.
 */
interface StockNetworkDataSource {

    suspend fun getQuote(symbol: String): QuoteResponse

    suspend fun searchSymbols(query: String): SymbolSearchResponse

    suspend fun getCompanyProfile(symbol: String): CompanyProfileResponse
}
