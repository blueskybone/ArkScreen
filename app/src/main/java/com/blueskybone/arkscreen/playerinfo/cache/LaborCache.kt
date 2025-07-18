package com.blueskybone.arkscreen.playerinfo.cache

/**
 *   Created by blueskybone
 *   Date: 2025/2/6
 */
data class LaborCache(
    var lastSyncTs: Long,
    var remainSec: Long,
    var max: Int,
    var current: Int,
    var isnull: Boolean
) {
    companion object {
        fun default(): LaborCache {
            return LaborCache(0L, 0L, 0, 0, true)
        }
    }
}