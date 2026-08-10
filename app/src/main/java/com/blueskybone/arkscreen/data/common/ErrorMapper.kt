package com.blueskybone.arkscreen.data.common

import com.blueskybone.arkscreen.domain.AppError

fun Throwable.toAppError(): AppError {
    return when (this) {
        is AppError -> this

        is java.net.UnknownHostException,
        is java.net.ConnectException -> AppError.NetworkUnavailable

        is java.net.SocketTimeoutException -> AppError.Timeout

        is retrofit2.HttpException -> {
            when (val code = this.code()) {
                401, 403 -> AppError.AuthExpired
                in 500..599 -> AppError.Server(
                    code = code,
                    msg = "服务器异常，请稍后再试"
                )
                else -> AppError.Server(
                    code = code,
                    msg = this.message()
                )
            }
        }
        is com.fasterxml.jackson.core.JsonProcessingException -> {
            AppError.DataParse
        }

        else -> AppError.Unknown(this)
    }
}