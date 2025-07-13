package com.blueskybone.arkscreen.network.model

data class BasicInfoResponse(
    val status:Int,
    val meg:String,
    val data: BasicInfoData
)
data class BasicInfoData(
    val uid:String,
    val guest:Int,
    val channelMasterId:Int,
    val nickName: String
)