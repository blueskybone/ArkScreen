package com.blueskybone.arkscreen.ui.gacha.model

/**
 * Created by blueskybone
 * Date: 2026/2/25
 */

/**
 * 所有卡池的最终汇总信息，
 * @property allPools 存储所有汇总的Gacha信息
 * @property dateRange 寻访记录的日期跨度
 * @property totalSum 寻访总数
 * @property rare6Count 6星寻访总数
 */
data class GachaSummary(
    val allPools: List<GachaPool> = emptyList(),
    val dateRange: String = "-",
    val totalSum: Int = 0,
    val rare6Count: Int = 0
)