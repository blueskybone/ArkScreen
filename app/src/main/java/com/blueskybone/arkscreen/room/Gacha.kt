package com.blueskybone.arkscreen.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */

/*
* 手写序列化反序列化
* 需要手动维护，不稳定性高，封装一下
* */
@Entity(indices = [Index(value = ["uid"])])
data class Gacha(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String,
    val ts: Long,
    val pool: String,
    var record: String, //val records: List<Record>,
    //val isValid: Boolean        //排除数据不相连错误
)

data class Gachas(
    val pool: String,
    var count: Int = 0,
    var ts: Long = 0L,
    var isFes:Boolean = false,
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
    val count: Int
)