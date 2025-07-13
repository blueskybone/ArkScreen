package com.blueskybone.arkscreen.network.model

data class BindingResponse(
    val code: Int,
    val message: String,
    val data: BindingData
)

data class BindingData(
    val list: List<BindingItem>
)

data class BindingItem(
    val appCode: String,
    val bindingList: List<BindingDetail>
)

data class BindingDetail(
    val nickName: String,
    val channelMasterId: String,
    val uid: String,
    val isOfficial: Boolean
)