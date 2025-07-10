package com.blueskybone.arkscreen.playerinfo.cache


//if completeTime == -1, count == count, else check if now > completeTime true + 1
data class RefreshCache(
    val lastSyncTs: Long,
    val max: Int = 3,
    val count: Int,
    val completeTime: Long,
    val isNull: Boolean
) {
    companion object{
        fun default(): RefreshCache {
            return RefreshCache(0L, 3, 0, 0L, true)
        }
    }

}