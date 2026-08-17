package com.blueskybone.arkscreen.data.common

import com.blueskybone.arkscreen.domain.common.domainResultOf
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> repositoryResultOf(block: suspend () -> T): Result<T> =
    domainResultOf(block).fold(
        onSuccess = Result.Companion::success,
        onFailure = { Result.failure(it.toAppError()) },
    )

fun <T> repositoryResult(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e.toAppError())
}
