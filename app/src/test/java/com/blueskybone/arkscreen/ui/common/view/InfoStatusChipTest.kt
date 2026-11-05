package com.blueskybone.arkscreen.ui.common.view

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class InfoStatusChipTest {
    @Test
    fun attendanceDateUsesShanghaiMidnight() {
        val now = Instant.parse("2026-11-05T16:10:00Z")
        assertFalse(attendedToday(Instant.parse("2026-11-05T15:59:00Z").epochSecond, now))
        assertTrue(attendedToday(Instant.parse("2026-11-05T16:01:00Z").epochSecond, now))
    }

    @Test
    fun noSuccessfulAttendanceIsNotAttended() {
        assertFalse(attendedToday(0L))
    }
}
