package com.blueskybone.arkscreen.playerinfo.cache

/**
 *   Created by blueskybone
 *   Date: 2025/9/17
 */

data class MeetCache(
    var lastSyncTs: Long,
    var completeTime: Long,
    var stats: Int,     //状态：0 idel, 1 on, 2 complete
    var isnull: Boolean
) {
    companion object {
        fun default(): MeetCache {
            return MeetCache(0L, 0L, 0, true)
        }
    }
}