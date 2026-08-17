package com.blueskybone.arkscreen.data.common

import com.blueskybone.arkscreen.domain.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMapperTest {

    @Test
    fun `wrapped dns failure maps to network unavailable`() {
        val error = Exception(
            "获取卡池类型失败: Unable to resolve host",
            UnknownHostException("Unable to resolve host"),
        )

        assertSame(AppError.NetworkUnavailable, error.toAppError())
    }

    @Test
    fun `wrapped timeout maps to timeout`() {
        val error = Exception(
            "获取凭证失败: timeout",
            SocketTimeoutException("timeout"),
        )

        assertSame(AppError.Timeout, error.toAppError())
    }

    @Test
    fun `http 404 maps to server error instead of network unavailable`() {
        val mapped = HttpStatusException(404, "资源请求失败：HTTP 404").toAppError()

        assertTrue(mapped is AppError.Server)
        assertEquals(404, (mapped as AppError.Server).code)
        assertEquals("资源请求失败：HTTP 404", mapped.message)
    }
}
