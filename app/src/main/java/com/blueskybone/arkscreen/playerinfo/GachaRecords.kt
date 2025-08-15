package com.blueskybone.arkscreen.playerinfo

data class Gachas(
    val pool: String,
    var count: Int = 0,
    var ts: Long = 0L,
    var isFes: Boolean = false,
    var data: MutableList<Records> = mutableListOf()
)

//导入导出：时间，卡池，记录
//导入：json，读取data列表，插入数据库

data class Record(
    val name: String,
    val rarity: Int,
    val isNew: Boolean
)

data class Records(
    val id: Int,
    val name: String,
    val charId: String,
    val isNew: Boolean,
    val count: Int,
    val ts:Long
)