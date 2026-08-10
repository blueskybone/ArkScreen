package com.blueskybone.arkscreen.ui.gacha.model

/**
 * 用于统计卡池信息的数据类
 * @property poolName 卡池名称
 * @property poolId 卡池ID
 * @property isFes 是否为限定卡池    //TODO:换成GACHATYPE
 * 剩下的字段分别为6星，5星，4星，3星的抽数
 */
data class GachaPoolStats (
    val poolName: String,
    val poolId: String,
    val isFes: Boolean,
    val rare6: Int,
    val rare5: Int,
    val rare4: Int,
    val rare3: Int,
)