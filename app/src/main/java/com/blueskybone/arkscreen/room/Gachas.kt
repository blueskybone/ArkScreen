package com.blueskybone.arkscreen.room

/**
 *   Created by blueskybone
 *   Date: 2025/2/3
 */
data class Gachas(
    val pool: String,
    var count: Int = 0,
    var ts: Long = 0L,
    var isFes:Boolean = false,
    var data: MutableList<Records> = mutableListOf()
)