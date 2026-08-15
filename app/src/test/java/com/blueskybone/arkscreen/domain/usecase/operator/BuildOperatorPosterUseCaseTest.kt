package com.blueskybone.arkscreen.domain.usecase.operator

import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.operator.Operator
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildOperatorPosterUseCaseTest {

    private val account = AccountSk(
        uid = "1",
        nickName = "Doctor",
        token = "",
        official = true,
        dId = "",
        channelMasterId = "",
    )

    @Test
    fun invoke_keepsOnlySixStarEliteTwoAndGroupsByProfession() {
        val operators = listOf(
            operator("eligible", rarity = 5, phase = 2, profession = "PIONEER"),
            operator("fiveStar", rarity = 4, phase = 2, profession = "PIONEER"),
            operator("eliteOne", rarity = 5, phase = 1, profession = "PIONEER"),
        )

        val poster = BuildOperatorPosterUseCase()(account, operators)

        assertEquals(1, poster.operatorCount)
        assertEquals(
            listOf("eligible"),
            poster.rows.first { it.profession == "PIONEER" }.operators.map { it.name },
        )
        assertEquals(8, poster.rows.size)
    }

    @Test
    fun invoke_sortsProfessionRowsByTrainingProgress() {
        val operators = listOf(
            operator("low", level = 60, potential = 5),
            operator("high", level = 90, potential = 0),
        )

        val names = BuildOperatorPosterUseCase()(account, operators)
            .rows.first { it.profession == "PIONEER" }
            .operators.map { it.name }

        assertEquals(listOf("high", "low"), names)
    }

    private fun operator(
        name: String,
        rarity: Int = 5,
        phase: Int = 2,
        profession: String = "PIONEER",
        level: Int = 90,
        potential: Int = 0,
    ) = Operator(
        name = name,
        rarity = rarity,
        evolvePhase = phase,
        profession = profession,
        level = level,
        potentialRank = potential,
    )
}
