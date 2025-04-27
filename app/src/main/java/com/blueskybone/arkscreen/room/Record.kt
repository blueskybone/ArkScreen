package com.blueskybone.arkscreen.room

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */

//导入导出：时间，卡池，记录
//导入：json，读取data列表，插入数据库

data class Record(
    val name: String,
    val rarity: Int,
    val isNew: Boolean
)