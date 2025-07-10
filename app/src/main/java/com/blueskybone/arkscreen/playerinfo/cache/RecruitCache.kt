package com.blueskybone.arkscreen.playerinfo.cache


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