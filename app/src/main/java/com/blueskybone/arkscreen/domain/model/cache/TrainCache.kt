package com.blueskybone.arkscreen.domain.model.cache

/**
 *   Created by blueskybone
 *   Date: 2025/9/17
 */
// 缓存保存上次状态：-1L 表示空闲，0L 表示专精完成，其他值表示专精中。
// 数据为空时展示“暂无数据”。专精中需要比较 completeTime 与 currentTs，并展示剩余时间。

data class TrainCache(
    var lastSyncTs: Long,
    var trainee: String,
    var status: Long,
    var completeTime: Long,
    var isnull: Boolean,
) {
    companion object {
        fun default(): TrainCache {
            return TrainCache(0L,"-",0L,0L, true)
        }
    }
}
