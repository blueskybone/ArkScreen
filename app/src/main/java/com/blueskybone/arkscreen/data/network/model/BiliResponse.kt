package com.blueskybone.arkscreen.data.network.model

import com.fasterxml.jackson.annotation.JsonAlias

data class BiliResponse(
    val code: Int,
    @JsonAlias("message")
    val msg: String = "",
    val data: BiliItem? = null,
)

data class BiliItem(
    val item: List<BiliVideoInfo> = emptyList(),
)

data class BiliVideoInfo(
    val cover: String,
    val bvid: String
)
