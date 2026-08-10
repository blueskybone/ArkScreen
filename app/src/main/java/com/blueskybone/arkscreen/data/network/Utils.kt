package com.blueskybone.arkscreen.data.network

import com.blueskybone.arkscreen.data.common.HttpStatusException
import org.json.JSONObject
import retrofit2.Response

/**
 * 封装统一的网络请求处理逻辑
 * @param call 挂起函数，返回 Retrofit 的 Response
 * @param errorMessage 业务层定义的错误前缀
 */
suspend fun <T> safeApiCall(
    call: suspend () -> Response<T>,
    errorMessage: String = "请求失败"
): T {
    val response = try {
        call()
    } catch (e: Exception) {
        throw Exception("$errorMessage: ${e.message}", e)
    }
    if (response.isSuccessful) {
        // 成功，返回 Body，如果 Body 为空，抛出异常
        return response.body() ?: throw Exception("$errorMessage: 返回数据为空")
    } else {
        // HTTP 报错（401, 500等）
        throw HttpStatusException(
            statusCode = response.code(),
            message = "$errorMessage: ${response.code()}: ${response.getErrorMessage()}",
        )
    }
}

//通用的扩展函数
fun <T> Response<T>.unwrap(): T {
    if (this.isSuccessful) {
        return this.body() ?: throw Exception("返回体为空")
    } else {
        throw HttpStatusException(this.code(), "网络请求失败: ${this.code()}")
    }
}

// 通用的解析错误返回的函数
fun <T> Response<T>.getErrorMessage(): String {
    val errorBodyString =
        this.errorBody()?.string() ?: return "未知网络错误 (${this.code()})"

    return try {
        val json = JSONObject(errorBodyString)
        // 依次尝试获取 message, msg
        when {
            json.has("message") && !json.isNull("message") -> json.getString("message")
            json.has("msg") && !json.isNull("msg") -> json.getString("msg")
            else -> "请求失败 (${this.code()})"
        }
    } catch (e: Exception) {
        // 如果不是 JSON 格式（比如返回了 HTML 报错页），回退到 HTTP 状态信息
        this.message().ifEmpty { "HTTP Error: ${this.code()}" }
    }
}
