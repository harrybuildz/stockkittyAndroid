package com.stockkitty.core.common.network

import javax.inject.Qualifier

/**
 * Qualifies which coroutine dispatcher an injection site expects, so that
 * tests can substitute deterministic dispatchers for the real ones.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: StockKittyDispatchers)

enum class StockKittyDispatchers {
    Default,
    IO,
}
