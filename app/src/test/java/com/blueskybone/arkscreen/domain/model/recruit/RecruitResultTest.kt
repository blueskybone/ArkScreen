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
    fun `one star does not lower four star guarantee`() {
        val result = RecruitResult(
            operators = listOf(operator("four", 4), operator("robot", 1))
        )

        assertEquals(4, result.rare)
    }

    @Test
    fun `one star does not lower five star guarantee`() {
        val result = RecruitResult(
            operators = listOf(operator("five", 5), operator("robot", 1))
        )

        assertEquals(5, result.rare)
    }

    @Test
    fun `pure robot result has one star guarantee`() {
        assertEquals(1, RecruitResult(operators = listOf(operator("robot", 1))).rare)
    }

    @Test
    fun `rare is zero only when operator list is empty`() {
        assertEquals(0, RecruitResult().rare)
    }

    @Test
    fun `display ranking keeps robot between four and three stars`() {
        val orderedRarities = listOf(2, 3, 1, 4, 5, 6)
            .map { rarity -> RecruitResult(operators = listOf(operator("ope", rarity))) }
            .sortedByDescending(RecruitResult::rankingScore)
            .map(RecruitResult::rare)

        assertEquals(listOf(6, 5, 4, 1, 3, 2), orderedRarities)
    }

    @Test
    fun `unknown rarity has lowest guarantee and display rank`() {
        val result = RecruitResult(operators = listOf(operator("unknown", 0)))

        assertEquals(0, result.rare)
        assertEquals(0, result.rankingScore)
    }

    private fun operator(name: String, rarity: Int) = RecruitOpe(
        name = name,
        rare = rarity,
        tags = emptyList(),
        skin = "",
    )
}
