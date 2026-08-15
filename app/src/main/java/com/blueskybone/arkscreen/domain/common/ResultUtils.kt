package com.blueskybone.arkscreen.domain.common

import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> domainResultOf(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
