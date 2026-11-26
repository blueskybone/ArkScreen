package com.blueskybone.arkscreen.domain.model.gacha


data class EndfieldGachaPool(
    val roleId: String,
    val type: String,
    val poolId: String,
    val poolName: String,
    val up6Ids: String,
    val officialCount: Int? = null,
)
