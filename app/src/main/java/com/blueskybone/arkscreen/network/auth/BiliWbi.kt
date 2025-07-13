package com.blueskybone.arkscreen.network.auth

import com.blueskybone.arkscreen.util.getJsonContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder



var wbiParams: WbiParams? = null

private val mixinKeyEncTab = intArrayOf(
    46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
    33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
    61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
    36, 20, 34, 44, 52
)

//getBiliWbi()全局调用一次，获取WbiParams对象（令牌有效时间3天）
suspend fun getBiliWbi(): WbiParams {
    val response = getWebTicket()
    val img = extractImageId(getJsonContent(response, "img"))
    val sub = extractImageId(getJsonContent(response, "sub"))
    return WbiParams(img, sub)
}

data class WbiParams(
    val imgKey: String,
    val subKey: String,
) {
    private val mixinKey: String
        get() = (imgKey + subKey).let { s ->
            buildString {
                repeat(32) {
                    append(s[mixinKeyEncTab[it]])
                }
            }
        }

    // 创建对象之后, 直接调用此函数处理
    fun enc(params: Map<String, Any?>): String {
        val sorted = params.filterValues { it != null }.toSortedMap()
        return buildString {
            append(sorted.toQueryString())
            val wts = System.currentTimeMillis() / 1000
            sorted["wts"] = wts
            append("&wts=")
            append(wts)
            append("&w_rid=")
            append((sorted.toQueryString() + mixinKey).toMD5())
        }
    }
}

suspend fun getWebTicket(): String {
    val timestamp = System.currentTimeMillis() / 1000
    val signature = hmacSha256("XgwSnGZ1p", "ts$timestamp")

    val url = "https://api.bilibili.com/bapis/bilibili.api.ticket.v1.Ticket/GenWebTicket?" +
            "key_id=ec02&" +
            "hexsign=$signature&" +
            "context[ts]=$timestamp&" +
            "csrf="

    val request = Request.Builder()
        .url(url)
        .addHeader(
            "user-agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0"
        )
        .post(FormBody.Builder().build())  // 空POST请求体
        .build()

    val client = OkHttpClient()
    return withContext(Dispatchers.IO) {
        client.newCall(request).execute().use {
            if (!it.isSuccessful) throw Exception("Unexpected code: ${it.code}")
            it.body?.string() ?: throw Exception("Empty response")
        }
    }
}

fun extractImageId(imgUrl: String): String {
    return imgUrl.substringAfterLast('/').substringBefore('.')
}

fun Any.encodeURIComponent(): String {
    return URLEncoder.encode(this.toString(), "UTF-8").replace("+", "%20")
}

fun Map<String, Any?>.toQueryString() =
    this.filterValues { it != null }.entries.joinToString("&") { (k, v) ->
        "${k.encodeURIComponent()}=${v!!.encodeURIComponent()}"
    }