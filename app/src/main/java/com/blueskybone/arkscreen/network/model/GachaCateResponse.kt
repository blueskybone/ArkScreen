package com.blueskybone.arkscreen.network.model

data class GachaCateResponse(
    val code: Int,
    val data:List<GachaCate>
)

data class GachaCate(
    val id: String,
    val name: String
)
