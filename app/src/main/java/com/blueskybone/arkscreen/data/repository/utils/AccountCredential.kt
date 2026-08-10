package com.blueskybone.arkscreen.data.repository.utils

/**
 * Created by blueskybone
 * Date: 2026/4/5
 */
data class SkCredential(
    val token: String,
    val dId: String? = null,
    val channelMasterId: String? = null
)

data class EfCredential(
    val token: String,
    val dId: String? = null,
    val channelMasterId: String? = null,
    val roleId: String? = null,
    val serverId: String? = null
)

data class GcCredential(
    val token: String,
    val akUserCenter: String,
    val xrToken: String,
    val channelMasterId: Int
)