package com.blueskybone.arkscreen.network.model

data class BasicInfoResponse(
    val code:Int,
    val msg:String,
    val data: BasicInfoData
)
data class BasicInfoData(
    val channelId:Int,
    val uid:String,
    val name: String
)