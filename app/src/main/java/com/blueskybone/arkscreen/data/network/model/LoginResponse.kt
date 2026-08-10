package com.blueskybone.arkscreen.data.network.model

data class LoginResponse(
    val code: Int,
    val message: String?,
    val data: LoginData
)

data class LoginData(
    val token: String  // 返回的 hgToken
)
