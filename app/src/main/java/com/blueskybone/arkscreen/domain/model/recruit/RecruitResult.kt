package com.blueskybone.arkscreen.domain.model.recruit

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */

data class RecruitResult(
    val tags: List<String> = listOf(),
    val operators: MutableList<RecruitOpe> = mutableListOf(),
    private var isSort: Boolean = false,
    var rare: Int = 0
) : Comparable<RecruitResult> {

    fun sort(downTo: Boolean = true) {
        if (downTo) operators.sortByDescending { operator -> operator.rare }
        else operators.sortBy { operator -> operator.rare }
        rare = rare()
        isSort = true
    }

    private fun rare(): Int {
        var r = 1
        for (operator in operators.toList()) {
            when (operator.rare) {
                6 -> return 6
                5 -> r = 5
                4 -> r = 4
                3 -> return 3
                2 -> return 2
            }
        }
        return r
    }

    //稀有度 > tag数目 > 人数
    override operator fun compareTo(other: RecruitResult): Int {
        if (!isSort) this.sort()
        if (!other.isSort) other.sort()
        return if (this.rare > other.rare) {
            -1
        } else if (this.rare < other.rare) {
            1
        } else {
            if (this.rare == 6 || this.rare == 5 || this.rare == 1) {
                return if (this.operators.size > other.operators.size) {
                    1
                } else if (this.operators.size < other.operators.size) {
                    -1
                } else {
                    if (this.tags.size > other.tags.size) {
                        1
                    } else -1
                }
            }
            if (this.operators.size < other.operators.size) {
                -1
            } else if (this.operators.size > other.operators.size) {
                1
            } else {
                if (this.tags.size < other.tags.size) {
                    -1
                } else 1
            }
        }
    }
}
