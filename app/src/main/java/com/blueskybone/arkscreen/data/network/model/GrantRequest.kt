package com.blueskybone.arkscreen.data.network.model

data class GrantRequest(
    val appCode: String,
    val token: String,
    val type: Int
)

