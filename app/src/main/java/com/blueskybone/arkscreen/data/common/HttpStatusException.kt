package com.blueskybone.arkscreen.data.common

/** 已收到 HTTP 响应，但状态码表示请求失败。 */
class HttpStatusException(
    val statusCode: Int,
    message: String,
) : Exception(message)
