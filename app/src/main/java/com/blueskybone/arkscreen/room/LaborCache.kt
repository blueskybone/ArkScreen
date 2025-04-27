package com.blueskybone.arkscreen.room

/**
 *   Created by blueskybone
 *   Date: 2025/2/6
 */
data class LaborCache(
    var lastUpdateTs: Long,
    var remainSec: Long,
    var max: Int,
    var current: Int,
    var isnull: Boolean
) {
    companion object {
        fun default(): LaborCache {
            return LaborCache(-1L, -1L, -1, -1, true)
        }
    }
}