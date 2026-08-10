package com.blueskybone.arkscreen.data.network.model

data class LoginResponse(
    val code: Int? = null,
    val message: String? = null,
    val status: Int? = null,
    val msg: String? = null,
    val data: LoginData? = null,
)

data class LoginData(
    val token: String  // 返回的 hgToken
)
