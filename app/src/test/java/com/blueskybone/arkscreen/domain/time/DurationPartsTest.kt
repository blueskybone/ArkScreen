package com.blueskybone.arkscreen.domain.time

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationPartsTest {

    @Test
    fun `negative duration is clamped to zero`() {
        assertEquals(
            DurationParts(0, 0, 0, 0, 0),
            durationParts(-1),
        )
    }

    @Test
    fun `duration is split at every unit boundary`() {
        assertEquals(
            DurationParts(1, 1, 1, 1, 1),
            durationParts(694_861),
        )
    }

    @Test
    fun `minute boundary moves seconds into minutes`() {
        assertEquals(DurationParts(0, 0, 0, 0, 59), durationParts(59))
        assertEquals(DurationParts(0, 0, 0, 1, 0), durationParts(60))
    }

    @Test
    fun `server day changes at midnight in China timezone`() {
        assertEquals(0, serverDayNumber(57_599))
        assertEquals(1, serverDayNumber(57_600))
    }
}
