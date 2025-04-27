package com.blueskybone.arkscreen.room

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */
data class ApCache(
    var lastUpdateTs: Long,
    var lastSyncTs: Long,
    var remainSec: Long,
    var max: Int,
    var current: Int,
    var isnull: Boolean
) {
    companion object {
        fun default(): ApCache {
            return ApCache(-1L, -1L, -1L, -1, -1, true)
        }
    }
}