package com.blueskybone.arkscreen.network.model

/**
 * 密码登录请求
 */
data class LoginRequest(
    val phone: String,
    val password: String
)

/**
 * 密码登录响应
 */
data class LoginResponse(
    val data: LoginData
)

data class LoginData(
    val token: String  // 返回的 hgToken
)
