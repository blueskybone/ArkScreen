package com.blueskybone.arkscreen.domain.model.gacha


data class EndfieldGachaRecord(
    val roleId: String,
    val type: String,
    val seqId: String,
    val poolId: String,
    val poolName: String,
    val poolType: String,
    val itemId: String,
    val itemName: String,
    val rarity: Int,
    val gachaTs: String,
    val kind: String,
    val isNew: Boolean,
    val isFree: Boolean,
    val poolVersion: Int? = null,
)
