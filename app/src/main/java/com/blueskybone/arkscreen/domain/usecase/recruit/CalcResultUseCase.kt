package com.blueskybone.arkscreen.domain.usecase.recruit

import com.blueskybone.arkscreen.domain.common.safeResultSync
import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase
import com.blueskybone.arkscreen.domain.model.recruit.RecruitOpe
import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult
import com.blueskybone.arkscreen.domain.repository.ResourceRepository
import com.blueskybone.arkscreen.domain.service.RecruitDatabaseProvider
import com.blueskybone.arkscreen.util.getOneCombination

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
    ): Result<List<RecruitResult>> = safeResultSync {
        if (tags.isEmpty()) {
            return@safeResultSync emptyList()
        }

        val db = databaseProvider.getDatabase().getOrThrow()

        val tagCombinations = getTagCombinations(tags)
        tagCombinations.map { tagCombination ->
            RecruitResult(
                tags = tagCombination,
                operators = query(
                    db = db,
                    tags = tagCombination,
                    filter = filter,
                ) as MutableList<RecruitOpe>,
            )
        }
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

    private fun query(
        db: RecruitDatabase,
        tags: List<String>,
        filter: Boolean,
    ): List<RecruitOpe> {
        if (tags.isEmpty()) return emptyList()

        if (filter) {
            if ("新手" in tags) return emptyList()
            if (matchesLowRarityOnly(db, tags)) return emptyList()
        }

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

    private fun matchesLowRarityOnly(
        db: RecruitDatabase,
        tags: List<String>,
    ): Boolean {
        return db.operatorLowList.any { operator ->
            operator.tag.containsAll(tags)
        }
    }
}