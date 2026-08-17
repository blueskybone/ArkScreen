package com.blueskybone.arkscreen.domain.model.cache

/**
 *   Created by blueskybone
 *   Date: 2025/9/17
 */
// completeTime 为 -1 时保持当前计数；否则在当前时间超过完成时间后将计数加一。
data class RecruitCache(
    val lastSyncTs: Long,
    val max: Int,
    val complete: Int,
    val completeTime: Long,
    val isNull: Boolean
) {
    companion object{
        fun default(): RecruitCache {
            return RecruitCache(0L, 0, 0, 0L, true)
        }
    }

}
