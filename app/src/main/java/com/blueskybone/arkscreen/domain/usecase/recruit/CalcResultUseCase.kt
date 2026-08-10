package com.blueskybone.arkscreen.domain.usecase.recruit

import com.blueskybone.arkscreen.domain.common.domainResultOf
import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase
import com.blueskybone.arkscreen.domain.model.recruit.RecruitOpe
import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult
import com.blueskybone.arkscreen.domain.service.RecruitDatabaseProvider

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */
class CalcResultUseCase(
    private val databaseProvider: RecruitDatabaseProvider,
) {

    suspend operator fun invoke(
        tags: List<String>,
        filter: Boolean = false,
    ): Result<List<RecruitResult>> = domainResultOf {
        if (tags.isEmpty()) {
            return@domainResultOf emptyList()
        }

        val db = databaseProvider.getDatabase().getOrThrow()

        val tagCombinations = getTagCombinations(tags)
        tagCombinations.mapNotNull { tagCombination ->
            val operators = query(
                db = db,
                tags = tagCombination,
            ).sortedWith(
                compareByDescending<RecruitOpe> { it.rare }
                    .thenBy { it.name }
            )
            if (operators.isEmpty()) return@mapNotNull null
            if (filter && !isUsefulQuickResult(operators)) return@mapNotNull null

            RecruitResult(
                tags = tagCombination,
                operators = operators,
            )
        }.sortedWith(
            compareByDescending<RecruitResult> { it.rankingScore }
                .thenByDescending { it.tags.size }
                .thenBy { it.operators.size }
                .thenBy { it.tags.joinToString() }
        )
    }

    private fun getTagCombinations(
        tags: List<String>,
        maxSize: Int = 3,
    ): List<List<String>> {
        return (maxSize downTo 1)
            .flatMap { size ->
                getOneCombination(tags, size)
            }
    }

    private fun getOneCombination(items: List<String>, size: Int): List<List<String>> {
        if (size !in 1..items.size) return emptyList()
        val result = mutableListOf<List<String>>()

        fun collect(start: Int, selected: MutableList<String>) {
            if (selected.size == size) {
                result += selected.toList()
                return
            }
            for (index in start until items.size) {
                selected += items[index]
                collect(index + 1, selected)
                selected.removeAt(selected.lastIndex)
            }
        }

        collect(0, mutableListOf())
        return result
    }

    private fun query(
        db: RecruitDatabase,
        tags: List<String>,
    ): List<RecruitOpe> {
        if (tags.isEmpty()) return emptyList()

        return buildList {
            if ("高级资深干员" in tags) {
                addAll(queryHighOperators(db, tags))
            }

            addAll(queryNormalOperators(db, tags))
            addAll(queryLowOperators(db, tags))
            addAll(queryRobotOperators(db, tags))
        }
    }

    private fun queryHighOperators(
        db: RecruitDatabase,
        tags: List<String>,
    ): List<RecruitOpe> {
        return db.operatorHighList
            .filter { operator -> operator.tag.containsAll(tags) }
            .map { operator ->
                RecruitOpe(
                    name = operator.name,
                    rare = operator.star,
                    tags = operator.tag,
                    skin = operator.skin,
                )
            }
    }

    private fun queryNormalOperators(
        db: RecruitDatabase,
        tags: List<String>,
    ): List<RecruitOpe> {
        return db.operatorList
            .filter { operator -> operator.tag.containsAll(tags) }
            .map { operator ->
                RecruitOpe(
                    name = operator.name,
                    rare = operator.star,
                    tags = operator.tag,
                    skin = operator.skin,
                )
            }
    }

    private fun queryLowOperators(
        db: RecruitDatabase,
        tags: List<String>,
    ): List<RecruitOpe> {
        return db.operatorLowList
            .filter { operator -> operator.tag.containsAll(tags) }
            .map { operator ->
                RecruitOpe(
                    name = operator.name,
                    rare = operator.star,
                    tags = operator.tag,
                    skin = operator.skin,
                )
            }
    }

    private fun queryRobotOperators(
        db: RecruitDatabase,
        tags: List<String>,
    ): List<RecruitOpe> {
        return db.operatorRobotList
            .filter { operator -> operator.tag.containsAll(tags) }
            .map { operator ->
                RecruitOpe(
                    name = operator.name,
                    rare = operator.star,
                    tags = operator.tag,
                    skin = operator.skin,
                )
            }
    }

    private fun isUsefulQuickResult(operators: List<RecruitOpe>): Boolean {
        val guaranteesFourStarsOrHigher = operators.all { it.rare >= 4 }
        val guaranteesRobot = operators.all { it.rare == 1 }
        return guaranteesFourStarsOrHigher || guaranteesRobot
    }
}
