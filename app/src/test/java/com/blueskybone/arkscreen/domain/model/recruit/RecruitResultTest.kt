package com.blueskybone.arkscreen.domain.model.recruit

import org.junit.Assert.assertEquals
import org.junit.Test

class RecruitResultTest {

    @Test
    fun `rare is the lowest result in recruit value order`() {
        val result = RecruitResult(
            operators = listOf(
                operator("six", 6),
                operator("five", 5),
                operator("four", 4),
            )
        )

        assertEquals(4, result.rare)
    }

    @Test
    fun `one star does not lower a mixed result below three stars`() {
        val result = RecruitResult(
            operators = listOf(
                operator("five", 5),
                operator("four", 4),
                operator("three", 3),
                operator("robot", 1),
            )
        )

        assertEquals(3, result.rare)
    }

    @Test
    fun `rare is zero only when operator list is empty`() {
        assertEquals(0, RecruitResult().rare)
    }

    @Test
    fun `one star result ranks ahead of four star result`() {
        val robot = RecruitResult(operators = listOf(operator("robot", 1)))
        val fourStar = RecruitResult(operators = listOf(operator("four", 4)))
        val threeStar = RecruitResult(operators = listOf(operator("three", 3)))

        assertEquals(true, robot.rankingScore > fourStar.rankingScore)
        assertEquals(true, fourStar.rankingScore > threeStar.rankingScore)
    }

    private fun operator(name: String, rarity: Int) = RecruitOpe(
        name = name,
        rare = rarity,
        tags = emptyList(),
        skin = "",
    )
}
