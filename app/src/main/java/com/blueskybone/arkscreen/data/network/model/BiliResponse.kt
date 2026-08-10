package com.blueskybone.arkscreen.data.network.model

data class BiliResponse(
    val code: Int,
    val msg: String,
    val data: BiliItem
)

data class BiliItem(
    val item: List<BiliVideoInfo>
)

data class BiliVideoInfo(
    val cover: String,
    val bvid: String
)