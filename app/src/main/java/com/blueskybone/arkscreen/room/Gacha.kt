package com.blueskybone.arkscreen.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */

//@Entity(indices = [Index(value = ["uid"])])
//data class Gacha(
//    @PrimaryKey(autoGenerate = true) val id: Long = 0,
//    val uid: String,
//    val ts: Long,
//    val pool: String,
//    var record: String, //val records: List<Record>,
//    //val isValid: Boolean        //排除数据不相连错误
//)


@Entity(indices = [Index(value = ["uid", "ts", "pos"], unique = true)])
data class Gacha(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var poolId: String = "UN",
    val poolCate: String = "UN",   //类型：“LIMITED" "CLASSIC" "NORMAL"
    val uid: String,
    val ts: Long,
    val pool: String,       //poolName
    val charName: String,
    val charId: String,
    val rarity: Int,
    val isNew: Boolean,
    var pos: Int = 0
)