package com.blueskybone.arkscreen.network.model

data class GachaResponse(
    val code: Int,
    val data: GachaData,
    val msg: String
)

data class GachaData(
    val list: List<RawRecord>,
    val hasMore: Boolean
)

data class RawRecord(
    val poolId:String,
    val poolName:String,
    val charId:String,
    val charName:String,
    val rarity:Int,
    val isNew:Boolean,
    val gachaTs:Long, //1754122170712
    val pos: Int,   //在1次抽卡中的位置，用于标记十连
)