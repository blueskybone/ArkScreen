package com.blueskybone.arkscreen.ui.gacha.model

/**
 * Created by blueskybone
 * Date: 2026/4/16
 */

/**
 * 所有卡池的最终汇总信息，用于第一页的卡片
 * @property poolCountNormal 普池已垫
 * @property poolCountFes 限定已垫
 * @property poolCountCore 中坚已垫
 * @property totalCount 总抽数
 * @property rare6Count 6星寻访总数
 */

data class GachaOverview(
    val poolCountNormal: Int = 0,
    val poolCountFes: Int = 0,
    val poolCountCore: Int = 0,
    val totalCount: Int = 0,
    val rare6Count: Int = 0,
    val dateRange: String = ""
)