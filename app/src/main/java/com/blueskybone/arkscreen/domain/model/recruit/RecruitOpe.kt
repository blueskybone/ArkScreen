package com.blueskybone.arkscreen.domain.model.recruit

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */
data class RecruitOpe(
    val name: String,
    val rare: Int,
    val tags: List<String>,
    val skin: String
) {
    operator fun compareTo(o: RecruitOpe): Int {
        return if (this.rare > o.rare) {
            -1
        } else if (this.rare < o.rare) {
            1
        } else 1
    }
}