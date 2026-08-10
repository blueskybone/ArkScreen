package com.blueskybone.arkscreen.domain.model.cache

/** Identifies the account that produced the currently stored realtime snapshot. */
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
