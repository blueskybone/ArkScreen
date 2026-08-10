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
 * @property hitRecords 该卡池用于展示的六星出货记录
 * @property totalCount 该卡池的全部寻访次数
 */
data class GachaPool(
    val poolName: String,
    val poolId: String,
    val isFes: Boolean,
    val hitRecords: List<Record> = emptyList(),
    val totalCount: Int = 0,
    val ts: Long
)
