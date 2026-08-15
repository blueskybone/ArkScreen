package com.blueskybone.arkscreen.data.network.model

sealed class BasicInfoRequest {
    // 参数1的请求格式
    data class OfficialRequest(
        val channelToken: String
    ) : BasicInfoRequest()

    // 参数2的请求格式
    data class BiliRequest(
        val token: String
    ) : BasicInfoRequest()
}
