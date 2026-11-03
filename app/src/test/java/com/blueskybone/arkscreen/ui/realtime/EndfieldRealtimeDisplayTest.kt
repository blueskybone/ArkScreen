package com.blueskybone.arkscreen.ui.realtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EndfieldRealtimeDisplayTest {
    @Test fun formatsTenThousandsWithoutTrailingZeroes() {
        assertEquals("12.3w", EndfieldRealtimeDisplay.tenThousands(123000))
        assertEquals("2895.35w", EndfieldRealtimeDisplay.tenThousands(28953521))
        assertEquals("3000w", EndfieldRealtimeDisplay.tenThousands(30000000))
        assertEquals("0w", EndfieldRealtimeDisplay.tenThousands(0))
        assertEquals("—", EndfieldRealtimeDisplay.tenThousands(null))
    }

    @Test fun progressUsesDomainTotalAndClampsOutOfRangeValues() {
        assertEquals(96, EndfieldRealtimeDisplay.progress(28953521, 30000000))
        assertEquals(100, EndfieldRealtimeDisplay.progress(32000000, 30000000))
        assertNull(EndfieldRealtimeDisplay.progress(1, 0))
    }
}
