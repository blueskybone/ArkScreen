package com.blueskybone.arkscreen.core.logger

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogSanitizerTest {
    @Test
    fun `redacts sensitive headers`() {
        val sanitized = LogSanitizer.sanitize(
            "Cookie: account=secret\nx-account-token: abc123\nContent-Type: application/json"
        )

        assertFalse(sanitized.contains("secret"))
        assertFalse(sanitized.contains("abc123"))
        assertTrue(sanitized.contains("Content-Type: application/json"))
    }

    @Test
    fun `redacts sensitive json and query values`() {
        val sanitized = LogSanitizer.sanitize(
            "{\"phone\":\"13800000000\",\"password\":\"secret\"} /login?token=abc&safe=yes"
        )

        assertFalse(sanitized.contains("13800000000"))
        assertFalse(sanitized.contains("secret"))
        assertFalse(sanitized.contains("token=abc"))
        assertTrue(sanitized.contains("safe=yes"))
    }
}
