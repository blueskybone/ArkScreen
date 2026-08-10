package com.blueskybone.arkscreen.network.model


data class LoginRequest(
    val phone: String,
    val password: String
)

data class LoginResponse(
    val code: Int,
    val message: String?,
    val data: LoginData?
)

data class LoginData(
    val token: String  // 返回的 hgToken
)
