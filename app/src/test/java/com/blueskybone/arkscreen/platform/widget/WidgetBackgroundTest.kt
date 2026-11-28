package com.blueskybone.arkscreen.platform.widget

import com.blueskybone.arkscreen.domain.AppError
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class WidgetBackgroundTest {
    @Test fun retryingAccountReadHasDeadline() = runBlocking {
        try {
            readWidgetAccounts(timeoutMillis = 50) { awaitCancellation() }
            fail("Expected timeout")
        } catch (_: WidgetAccountReadTimeout) { }
    }

    @Test fun missingAccountIsNotTimeout() = runBlocking {
        assertNull(readWidgetAccounts<String?> { null })
    }

    @Test fun callerCancellationIsNotConvertedToRetryableFailure() = runBlocking {
        var timedOut = false
        val job = launch {
            try { readWidgetAccounts(timeoutMillis = 50) { awaitCancellation() } }
            catch (_: WidgetAccountReadTimeout) { timedOut = true }
        }
        yield()
        job.cancelAndJoin()
        assertFalse(timedOut)
    }

    @Test fun retryOnlyTransientErrors() {
        assertTrue(isRetryableWidgetError(AppError.NetworkUnavailable))
        assertTrue(isRetryableWidgetError(AppError.Timeout))
        assertTrue(isRetryableWidgetError(AppError.Server(503, null)))
        assertTrue(isRetryableWidgetError(WidgetAccountReadTimeout()))
        assertFalse(isRetryableWidgetError(AppError.AuthExpired))
        assertFalse(isRetryableWidgetError(AppError.DataParse))
        assertFalse(isRetryableWidgetError(AppError.Business(400, "bad request")))
    }
}
