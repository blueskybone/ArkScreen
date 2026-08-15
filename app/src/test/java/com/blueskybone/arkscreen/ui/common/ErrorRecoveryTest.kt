package com.blueskybone.arkscreen.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorRecoveryTest {

    @Test
    fun `network failure mentioning cred is not treated as expired login`() {
        val message = "获取 cred 凭证失败: Unable to resolve host"

        assertEquals("网络不可用", userFacingError(message))
        assertEquals(ErrorRecovery.RETRY, recoveryFor(message))
    }

    @Test
    fun `explicit credential failure still requests relogin`() {
        val message = "cred 授权失败"

        assertEquals("登录状态已过期", userFacingError(message))
        assertEquals(ErrorRecovery.RELOGIN, recoveryFor(message))
    }
}
