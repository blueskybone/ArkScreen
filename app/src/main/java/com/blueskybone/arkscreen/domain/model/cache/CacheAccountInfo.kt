package com.blueskybone.arkscreen.domain.model.cache

/** 标识当前实时数据缓存由哪个账号生成，避免切换账号后展示旧账号快照。 */
data class CacheAccountInfo(
    val uid: String,
    val nickname: String,
    val official: Boolean,
) {
    companion object {
        fun empty() = CacheAccountInfo(
            uid = "",
            nickname = "",
            official = true,
        )
    }
}
