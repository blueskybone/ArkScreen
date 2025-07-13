package com.blueskybone.arkscreen.network.model

data class CredResponse(
    val code: Int,
    val message: String,
    val data: CredData
)

data class CredData(
    val cred: String,
    val token: String
)
