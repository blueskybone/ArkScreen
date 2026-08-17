package com.blueskybone.arkscreen.ui.character.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class CharNotOwnGridTest {

    @Test
    fun `remaining width is split evenly around complete columns`() {
        assertEquals(
            15,
            calculateGridHorizontalPadding(
                containerWidth = 360,
                cellWidth = 66,
                minPadding = 12,
            ),
        )
    }

    @Test
    fun `minimum padding is preserved when columns fit exactly`() {
        assertEquals(
            12,
            calculateGridHorizontalPadding(
                containerWidth = 420,
                cellWidth = 66,
                minPadding = 12,
            ),
        )
    }

    @Test
    fun `narrow container still keeps one column and minimum padding`() {
        assertEquals(
            12,
            calculateGridHorizontalPadding(
                containerWidth = 60,
                cellWidth = 66,
                minPadding = 12,
            ),
        )
    }

    @Test
    fun `invalid measurements return safe minimum padding`() {
        assertEquals(12, calculateGridHorizontalPadding(0, 66, 12))
        assertEquals(12, calculateGridHorizontalPadding(360, 0, 12))
    }
}
