package com.blueskybone.arkscreen.platform.widget

import com.blueskybone.arkscreen.domain.AppError
import java.io.IOException

internal fun isRetryableWidgetError(error: Throwable): Boolean = when (error) {
    is IOException, AppError.NetworkUnavailable, AppError.Timeout -> true
    is AppError.Server -> error.code in 500..599 || error.code == 429
    is AppError.Unknown -> isRetryableWidgetError(error.throwable)
    else -> false
}
