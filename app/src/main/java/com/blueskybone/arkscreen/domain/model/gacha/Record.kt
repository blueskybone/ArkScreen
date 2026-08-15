package com.blueskybone.arkscreen.domain.model.gacha
/**
 * Created by blueskybone
 * Date: 2026/3/18
 */

/**
 * 单条寻访记录
 * @property
 * */
data class Record(
    val id: String, //唯一标识符
    val pool: String,
    val poolId: String,
    val poolCate: String,
    var isFes: Boolean = false,
    val charName: String,
    val charId: String,
    val rarity: Int,
    val isNew: Boolean,
    val pos: Int,
    val ts: Long
)