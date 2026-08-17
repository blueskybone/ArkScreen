package com.blueskybone.arkscreen.data.common

import com.blueskybone.arkscreen.domain.AppError
import com.fasterxml.jackson.core.JsonProcessingException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toAppError(): AppError {
    val causes = generateSequence(this) { it.cause }.toList()
    causes.filterIsInstance<AppError>().firstOrNull()?.let { return it }

    return when {
        causes.any { it is SocketTimeoutException } -> AppError.Timeout

        causes.any { it is HttpStatusException } -> {
            val httpError = causes.filterIsInstance<HttpStatusException>().first()
            mapHttpStatus(httpError.statusCode, httpError.message)
        }

        causes.any { it is HttpException } -> {
            val httpError = causes.filterIsInstance<HttpException>().first()
            mapHttpStatus(httpError.code(), httpError.message())
        }

        causes.any {
            it is UnknownHostException ||
                it is ConnectException ||
                it is IOException
        } -> AppError.NetworkUnavailable

        causes.any { it is JsonProcessingException } -> AppError.DataParse
        else -> AppError.Unknown(this)
    }
}

private fun mapHttpStatus(code: Int, message: String?): AppError = when (code) {
    401, 403 -> AppError.AuthExpired
    in 500..599 -> AppError.Server(code, "服务器异常，请稍后再试")
    else -> AppError.Server(code, message ?: "HTTP $code")
}
