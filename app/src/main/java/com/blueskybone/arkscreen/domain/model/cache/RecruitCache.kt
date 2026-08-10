package com.blueskybone.arkscreen.domain.model.cache

/**
 *   Created by blueskybone
 *   Date: 2025/9/17
 */
//if completeTime == -1, count == count, else check if now > completeTime true + 1
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