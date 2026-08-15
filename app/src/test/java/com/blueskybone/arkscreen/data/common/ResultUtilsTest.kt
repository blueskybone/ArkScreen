package com.blueskybone.arkscreen.data.common

import com.blueskybone.arkscreen.domain.AppError
import com.blueskybone.arkscreen.domain.common.domainResultOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class ResultUtilsTest {

    @Test
    fun `domain result preserves original exception`() = runBlocking {
        val error = IllegalStateException("domain failure")

        val result = domainResultOf<Unit> { throw error }

        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `repository result maps IO exception to network error`() = runBlocking {
        val result = repositoryResultOf<Unit> { throw IOException("offline") }

        assertSame(AppError.NetworkUnavailable, result.exceptionOrNull())
    }

    @Test
    fun `repository result preserves existing app error`() = runBlocking {
        val error = AppError.Business(code = 10, msg = "业务失败")

        val result = repositoryResultOf<Unit> { throw error }

        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `cancellation is never converted to failure`() {
        val cancellation = CancellationException("cancelled")
        try {
            runBlocking {
                repositoryResultOf<Unit> { throw cancellation }
            }
            fail("CancellationException should be rethrown")
        } catch (error: CancellationException) {
            assertSame(cancellation, error)
            assertTrue(error.message == "cancelled")
        }
    }
}
