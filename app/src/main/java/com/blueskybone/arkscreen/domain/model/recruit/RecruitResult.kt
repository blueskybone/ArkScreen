package com.blueskybone.arkscreen.domain.model.recruit

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */

data class RecruitResult(
    val tags: List<String> = listOf(),
    val operators: List<RecruitOpe> = emptyList(),
) {

    /** Lowest guaranteed result using recruit value order: 6, 5, 4, 1, 3, 2. */
    val rare: Int
        get() = operators.minByOrNull { rarityRank(it.rare) }?.rare ?: 0

    val rankingScore: Int
        get() = rarityRank(rare)

    companion object {
        /** Comparison rank for recruit guarantees: 6 > 5 > 4 > 1 > 3 > 2. */
        fun rarityRank(rarity: Int): Int = when (rarity) {
            6 -> 6
            1 -> 5
            5 -> 4
            4 -> 3
            2 -> 2
            3 -> 1
            else -> 0
        }
    }
}
