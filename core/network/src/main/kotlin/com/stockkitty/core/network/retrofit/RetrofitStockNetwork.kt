package com.stockkitty.core.network.retrofit

import com.stockkitty.core.network.StockNetworkDataSource
import com.stockkitty.core.network.model.CompanyProfileResponse
import com.stockkitty.core.network.model.QuoteResponse
import com.stockkitty.core.network.model.SymbolSearchResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [StockNetworkDataSource] backed by Retrofit against the live Finnhub API.
 */
@Singleton
class RetrofitStockNetwork @Inject constructor(
    private val api: FinnhubApi,
) : StockNetworkDataSource {

    override suspend fun getQuote(symbol: String): QuoteResponse = api.getQuote(symbol)

    override suspend fun searchSymbols(query: String): SymbolSearchResponse =
        api.searchSymbols(query)

    override suspend fun getCompanyProfile(symbol: String): CompanyProfileResponse =
        api.getCompanyProfile(symbol)
}
