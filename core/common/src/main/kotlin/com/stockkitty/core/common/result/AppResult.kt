package com.stockkitty.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * A discriminated union of loading / success / error used to surface
 * asynchronous state to the UI layer without throwing across boundaries.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val exception: Throwable) : AppResult<Nothing>
    data object Loading : AppResult<Nothing>
}

/**
 * Wraps an upstream [Flow] into an [AppResult] stream, emitting [AppResult.Loading]
 * first and converting upstream failures into [AppResult.Error] instead of
 * cancelling collectors.
 */
fun <T> Flow<T>.asResult(): Flow<AppResult<T>> = map<T, AppResult<T>> { AppResult.Success(it) }
    .onStart { emit(AppResult.Loading) }
    .catch { emit(AppResult.Error(it)) }
