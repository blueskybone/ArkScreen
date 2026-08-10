package com.blueskybone.arkscreen.domain

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */

sealed class AppError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    data object NetworkUnavailable : AppError("网络不可用")
    data object Timeout : AppError("请求超时")
    data object AuthExpired : AppError("登录已过期")
    data object DataParse : AppError("数据解析失败")

    data class Server(
        val code: Int,
        val msg: String?
    ) : AppError(msg)

    data class Business(
        val code: Int? = null,
        val msg: String
    ) : AppError(msg)

    data class Unknown(
        val throwable: Throwable
    ) : AppError(throwable.message, throwable)
}
