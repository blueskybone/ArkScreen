package com.blueskybone.arkscreen.room

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */
data class ApCache(
    var lastUpdateTs: Long,     //感觉有点多余了
    var lastSyncTs: Long,       //
    var remainSec: Long,
    var recoverTime: Long,
    var max: Int,
    var current: Int,
    var isnull: Boolean
) {
    companion object {
        fun default(): ApCache {
            return ApCache(-1L, -1L, -1L, -1,-1, -1, true)
        }
    }
}

/* 重新考虑一下逻辑，始终记录上次sync同步的时间戳，以及recoverTime，感觉只有这两个能用。但还是要看无人机的算法
* 无人机的算法考虑了速度，总之按照时间推演的比例来计算已经恢复的无人机，但是remainSec这个数据是真的。只有当前的无人机需要推算一下数据，而currentTs可以随意取值，也就是说cache要保存的并不是上次同步的时间
* 而是只需要lastUpdateTs，current,max,remainSec/recoverTime,实际上官方没有也没有提供recoverTime
* 官方一直采用的是记录lastUpdateTime相关的数据，然后再用currentTs去做相关的事情
* 如果是这样的话，我的整个realtimeModel都要重写了。也许具体的算法应该在前端呈现，而不是在model中计算好，这样耦合度也会变低*/
/*
*         run {
            val laborNode = tree.at("/data/building/labor")
            val laborValue = laborNode["value"].asInt()
            val laborMax = laborNode["maxValue"].asInt()
            var laborCurrent = if (laborNode["remainSecs"].asLong() == 0L) {
                laborValue
            } else {
                ((currentTs - laborNode["lastUpdateTime"].asLong()) * (laborMax - laborValue)
                        / laborNode["remainSecs"].asLong() + laborValue).toInt()
            }
            var laborRemain =
                laborNode["remainSecs"].asInt() - (currentTs - laborNode["lastUpdateTime"].asInt())
            if (laborCurrent > laborMax) {
                laborCurrent = laborMax
            }
            val recoverTime =
                laborNode["remainSecs"].asInt() + laborNode["lastUpdateTime"].asInt()
            playerData.labor.current = laborCurrent
            playerData.labor.max = laborMax
            if (laborRemain < 0) {
                laborRemain = 0
            }

            playerData.labor.remainSecs = laborRemain
            playerData.labor.recoverTime = recoverTime.toLong()
        }
* */