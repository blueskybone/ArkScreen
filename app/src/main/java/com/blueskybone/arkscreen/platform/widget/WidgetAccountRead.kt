package com.blueskybone.arkscreen.platform.widget

import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

/** Bound one-shot reads of repository flows that otherwise retry indefinitely. */
internal suspend fun <T> readWidgetAccounts(timeoutMillis: Long = 3_000, block: suspend () -> T): T =
    (withTimeoutOrNull(timeoutMillis) { Result.success(block()) }
        ?: throw WidgetAccountReadTimeout()).getOrThrow()

internal class WidgetAccountReadTimeout : IOException("Widget account lookup timed out")
