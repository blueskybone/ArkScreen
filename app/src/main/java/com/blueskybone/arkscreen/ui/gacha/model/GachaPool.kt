package com.blueskybone.arkscreen.ui.gacha.model

/**
 * Created by blueskybone
 * Date: 2026/4/16
 */

/**
 * 卡池信息
 * @property poolName 卡池名称
 * @property poolId 卡池ID
 * @property isFes 是否是限定池（其实建议换成GACHATYPE）
 * @property records 该卡池的所有寻访记录
 * @property count 该卡池的抽数
 */
data class GachaPool(
    val poolName: String,
    val poolId: String,
    val isFes: Boolean,
    val records: List<Record> = emptyList(),
    val totalCount: Int = 0,
    val ts: Long
)