package com.blueskybone.arkscreen.data.network.model


// TODO：记得改名字，不要用login, 用token或者类似的表达。
data class LoginRequest(
    val phone: String,
    val password: String
)