package com.blueskybone.arkscreen.network.model

data class GrantResponse(
    val status: Int,
    val msg: String,
    val data: GrantData
)

data class GrantData(
    val code: String,
)