package com.stockkitty.core.network.retrofit

import com.stockkitty.core.network.model.CompanyProfileResponse
import com.stockkitty.core.network.model.QuoteResponse
import com.stockkitty.core.network.model.SymbolSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Finnhub REST endpoints used by the app. Authentication is applied by
 * [com.stockkitty.core.network.FinnhubAuthInterceptor], so no token
 * parameter appears here.
 */
interface FinnhubApi {

    @GET("quote")
    suspend fun getQuote(@Query("symbol") symbol: String): QuoteResponse

    @GET("search")
    suspend fun searchSymbols(@Query("q") query: String): SymbolSearchResponse

    @GET("stock/profile2")
    suspend fun getCompanyProfile(@Query("symbol") symbol: String): CompanyProfileResponse
}
