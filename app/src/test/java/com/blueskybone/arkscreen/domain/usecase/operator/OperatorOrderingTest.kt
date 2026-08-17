package com.blueskybone.arkscreen.domain.usecase.operator

import com.blueskybone.arkscreen.domain.model.operator.Operator
import org.junit.Assert.assertEquals
import org.junit.Test

class OperatorOrderingTest {

    @Test
    fun `sorts by rarity phase level profession and name`() {
        val operators = listOf(
            operator("低等级近卫", rarity = 5, phase = 2, level = 30, profession = "WARRIOR"),
            operator("高等级近卫", rarity = 5, phase = 2, level = 60, profession = "WARRIOR"),
            operator("先锋", rarity = 5, phase = 2, level = 60, profession = "PIONEER"),
            operator("低稀有度", rarity = 4, phase = 2, level = 90, profession = "PIONEER"),
        )

        assertEquals(
            listOf("先锋", "高等级近卫", "低等级近卫", "低稀有度"),
            operators.sortedWith(OperatorOrdering.default).map { it.name },
        )
    }

    private fun operator(
        name: String,
        rarity: Int,
        phase: Int,
        level: Int,
        profession: String,
    ) = Operator(
        name = name,
        rarity = rarity,
        evolvePhase = phase,
        level = level,
        profession = profession,
    )
}
