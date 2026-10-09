package com.stockkitty.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches the Finnhub API token to every outgoing request via the
 * X-Finnhub-Token header, keeping the key out of request URLs (and logs).
 */
class FinnhubAuthInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("X-Finnhub-Token", apiKey)
            .build()
        return chain.proceed(request)
    }
}
