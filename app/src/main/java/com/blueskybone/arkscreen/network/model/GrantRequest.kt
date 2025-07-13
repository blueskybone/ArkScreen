package com.blueskybone.arkscreen.network.model

data class GrantRequest(
    val appCode: String,
    val token: String,
    val type: Int
)

