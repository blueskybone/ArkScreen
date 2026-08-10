package com.blueskybone.arkscreen.data.repository.utils

import com.blueskybone.arkscreen.data.common.toAppError
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> safeResultSync(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e.toAppError())
}

fun <T> safeResultNormal(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e.toAppError())
}