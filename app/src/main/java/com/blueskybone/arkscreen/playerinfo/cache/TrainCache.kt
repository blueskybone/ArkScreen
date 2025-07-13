package com.blueskybone.arkscreen.playerinfo.cache


//缓存存储上一次的状态 -1L: 空闲中  0L:专精完成 else: 专精中
//if isnull == true -> 暂无数据
//if 空闲中 -> idle 专精完成 -> completed  专精中: 计算是否已完成：if 未完成：计算completeTime 和 currentTs差距，显示时间。

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