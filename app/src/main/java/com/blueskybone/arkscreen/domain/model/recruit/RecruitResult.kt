package com.blueskybone.arkscreen.domain.model.recruit

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */

data class RecruitResult(
    val tags: List<String> = listOf(),
    val operators: List<RecruitOpe> = emptyList(),
) {

    /** 组合的最低保底稀有度；1 星小车不会拉低原本 4 星及以上的保底。 */
    val rare: Int
        get() = operators.minByOrNull { guaranteeRank(it.rare) }?.rare ?: 0

    /** 结果展示顺序：6 > 5 > 4 > 1 > 3 > 2。 */
    val rankingScore: Int
        get() = displayRank(rare)

    companion object {
        /** 仅用于选择最低保底：6 > 1 > 5 > 4 > 2 > 3。 */
        fun guaranteeRank(rarity: Int): Int = when (rarity) {
            6 -> 6
            1 -> 5
            5 -> 4
            4 -> 3
            2 -> 2
            3 -> 1
            else -> 0
        }

        /** 用于完整结果之间的排序：6 > 5 > 4 > 1 > 3 > 2。 */
        fun displayRank(rarity: Int): Int = when (rarity) {
            6 -> 6
            5 -> 5
            4 -> 4
            1 -> 3
            3 -> 2
            2 -> 1
            else -> 0
        }
    }
}
