package com.blueskybone.arkscreen.domain.usecase.recruit

import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase
import com.blueskybone.arkscreen.domain.service.RecruitDatabaseProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalcResultUseCaseTest {

    @Test
    fun `quick filter keeps mixed robot and four star result`() = runBlocking {
        val useCase = CalcResultUseCase(
            provider(
                normal = listOf(operator("four", 4, "爆发")),
                robots = listOf(robot("robot", 1, "爆发")),
            )
        )

        val results = useCase(listOf("爆发"), filter = true).getOrThrow()

        assertEquals(1, results.size)
        assertEquals(listOf(4, 1), results.single().operators.map { it.rare })
        assertEquals(4, results.single().rare)
    }

    @Test
    fun `quick filter rejects result containing ordinary three star`() = runBlocking {
        val useCase = CalcResultUseCase(
            provider(
                normal = listOf(
                    operator("four", 4, "特种干员"),
                    operator("three", 3, "特种干员"),
                ),
                robots = listOf(robot("robot", 1, "特种干员")),
            )
        )

        val results = useCase(listOf("特种干员"), filter = true).getOrThrow()

        assertTrue(results.isEmpty())
    }

    @Test
    fun `quick filter keeps pure robot result`() = runBlocking {
        val useCase = CalcResultUseCase(
            provider(robots = listOf(robot("robot", 1, "支援机械")))
        )

        val results = useCase(listOf("支援机械"), filter = true).getOrThrow()

        assertEquals(1, results.single().rare)
    }

    @Test
    fun `quick filter rejects two star guarantee`() = runBlocking {
        val useCase = CalcResultUseCase(
            provider(normal = listOf(operator("two", 2, "近战位")))
        )

        val results = useCase(listOf("近战位"), filter = true).getOrThrow()

        assertTrue(results.isEmpty())
    }

    @Test
    fun `quick filter uses final guarantee instead of highest operator`() = runBlocking {
        val useCase = CalcResultUseCase(
            provider(
                normal = listOf(
                    operator("six", 6, "输出"),
                    operator("five", 5, "输出"),
                    operator("three", 3, "输出"),
                ),
                robots = listOf(robot("robot", 1, "输出")),
            )
        )

        val results = useCase(listOf("输出"), filter = true).getOrThrow()

        assertTrue(results.isEmpty())
    }

    @Test
    fun `normal calculation retains low rarity results`() = runBlocking {
        val useCase = CalcResultUseCase(
            provider(normal = listOf(operator("three", 3, "输出")))
        )

        val results = useCase(listOf("输出"), filter = false).getOrThrow()

        assertEquals(3, results.single().rare)
    }

    @Test
    fun `operators within result are sorted by numeric rarity descending`() = runBlocking {
        val useCase = CalcResultUseCase(
            provider(
                normal = listOf(
                    operator("three", 3, "爆发"),
                    operator("five", 5, "爆发"),
                    operator("four", 4, "爆发"),
                ),
                robots = listOf(robot("robot", 1, "爆发")),
            )
        )

        val result = useCase(listOf("爆发"), filter = false).getOrThrow().single()

        assertEquals(listOf(5, 4, 3, 1), result.operators.map { it.rare })
    }

    private fun provider(
        normal: List<RecruitDatabase.Operator> = emptyList(),
        robots: List<RecruitDatabase.OperatorRobot> = emptyList(),
    ): RecruitDatabaseProvider = object : RecruitDatabaseProvider {
        override suspend fun getDatabase() = Result.success(
            RecruitDatabase(
                newOpe = RecruitDatabase.NewOpe(emptyList()),
                operatorHighList = emptyList(),
                operatorList = normal,
                operatorLowList = emptyList(),
                operatorRobotList = robots,
                update = RecruitDatabase.Update("", ""),
            )
        )

        override fun clearCache() = Unit
    }

    private fun operator(name: String, star: Int, tag: String) =
        RecruitDatabase.Operator(name, star, listOf(tag), "")

    private fun robot(name: String, star: Int, tag: String) =
        RecruitDatabase.OperatorRobot(name, star, listOf(tag), "")
}
