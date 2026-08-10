package com.blueskybone.arkscreen.ui.gacha.model

/**
 * Created by blueskybone
 * Date: 2026/3/9
 */

/**
 * 单条寻访记录。主要添加了一个count。
 * @property id 用于标识不同的记录
 * @property name 干员名称
 * @property charId 干员id
 * @property isNew 是否为新干员
 * @property count 标识当前寻访记录在一轮出货的抽数
 * @property ts 寻访时间
 * @property rare 稀有度
 * @property gachaPool 所属卡池
 * @property gachaCount 所属卡池抽数
 */

data class Record(
    val id: String,
    val name: String,
    val charId: String,
    val isNew: Boolean,
    val count: Int,
    val ts:Long,
    val rare: Int,
    val gachaPool: String,
    val gachaCount: Int
)