package com.blueskybone.arkscreen.playerinfo.cache

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */
data class ApCache(
    var lastSyncTs: Long,
    var remainSec: Long,
    var recoverTime: Long,
    var max: Int,
    var current: Int,
    var isnull: Boolean
) {
    companion object {
        fun default(): ApCache {
            return ApCache(0L, 0L, 0, 0, 0, true)
        }
    }
}