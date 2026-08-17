package com.blueskybone.arkscreen.domain.model.operator

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
data class OperatorBasicInfo(
    val charId: String,
    val name: String,
    val rarity: Int,
    val profession: String,
    val skinId: String = "$charId#1",
)