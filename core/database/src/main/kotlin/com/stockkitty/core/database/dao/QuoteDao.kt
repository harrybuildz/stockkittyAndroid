package com.stockkitty.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.stockkitty.core.database.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Query("SELECT * FROM quotes")
    fun observeAll(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE symbol = :symbol")
    fun observeBySymbol(symbol: String): Flow<QuoteEntity?>

    @Upsert
    suspend fun upsert(entities: List<QuoteEntity>)

    @Query("DELETE FROM quotes WHERE symbol = :symbol")
    suspend fun delete(symbol: String)
}
