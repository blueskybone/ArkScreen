package com.blueskybone.arkscreen.data.network.model

data class CredResponse(
    val code: Int,
    val message: String,
    val data: CredData
)

data class CredData(
    val cred: String,
    val token: String
)
