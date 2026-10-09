package com.stockkitty.core.data.testdoubles

import com.stockkitty.core.database.dao.QuoteDao
import com.stockkitty.core.database.dao.WatchlistDao
import com.stockkitty.core.database.entity.QuoteEntity
import com.stockkitty.core.database.entity.WatchlistEntity
import com.stockkitty.core.network.StockNetworkDataSource
import com.stockkitty.core.network.model.CompanyProfileResponse
import com.stockkitty.core.network.model.QuoteResponse
import com.stockkitty.core.network.model.SymbolSearchResponse
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeWatchlistDao : WatchlistDao {

    val entities = MutableStateFlow<List<WatchlistEntity>>(emptyList())

    override fun observeAll(): Flow<List<WatchlistEntity>> = entities.asStateFlow()

    override fun observeIsWatched(symbol: String): Flow<Boolean> =
        entities.map { list -> list.any { it.symbol == symbol } }

    override suspend fun getSymbols(): List<String> = entities.value.map { it.symbol }

    override suspend fun insert(entity: WatchlistEntity) {
        entities.update { current ->
            if (current.any { it.symbol == entity.symbol }) current else current + entity
        }
    }

    override suspend fun delete(symbol: String) {
        entities.update { current -> current.filterNot { it.symbol == symbol } }
    }
}

class FakeQuoteDao : QuoteDao {

    val entities = MutableStateFlow<Map<String, QuoteEntity>>(emptyMap())

    override fun observeAll(): Flow<List<QuoteEntity>> = entities.map { it.values.toList() }

    override fun observeBySymbol(symbol: String): Flow<QuoteEntity?> =
        entities.map { it[symbol] }

    override suspend fun upsert(entities: List<QuoteEntity>) {
        this.entities.update { current -> current + entities.associateBy { it.symbol } }
    }

    override suspend fun delete(symbol: String) {
        entities.update { it - symbol }
    }
}

class FakeStockNetwork : StockNetworkDataSource {

    val quotes = mutableMapOf<String, QuoteResponse>()
    val failingSymbols = mutableSetOf<String>()
    var searchResponse = SymbolSearchResponse()
    var profileResponse = CompanyProfileResponse()

    override suspend fun getQuote(symbol: String): QuoteResponse {
        if (symbol in failingSymbols) throw IOException("Network error for $symbol")
        return quotes.getValue(symbol)
    }

    override suspend fun searchSymbols(query: String): SymbolSearchResponse = searchResponse

    override suspend fun getCompanyProfile(symbol: String): CompanyProfileResponse =
        profileResponse
}
