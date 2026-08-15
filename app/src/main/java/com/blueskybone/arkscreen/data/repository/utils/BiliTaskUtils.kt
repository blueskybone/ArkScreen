package com.blueskybone.arkscreen.data.repository.utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.TreeMap

fun appSign(
    params: MutableMap<String, String>,
    appkey: String = "1d8b6e7d45233436",
    appsec: String = "560c52ccd288fed045859ed18bffd973"
): Map<String, String> {
    // 添加appkey
    params["appkey"] = appkey
    // 按照key排序
    val sortedParams = TreeMap(params)
    // 构建查询字符串
    val query = buildQuery(sortedParams)
    // 计算签名
    val sign = md5("$query$appsec")
    sortedParams["sign"] = sign
    return sortedParams
}


//构建查询字符串
fun buildQuery(params: Map<String, String>): String {
    return params.entries.joinToString("&") { (key, value) ->
        "${URLEncoder.encode(key, StandardCharsets.UTF_8.name())}=${
            URLEncoder.encode(
                value,
                StandardCharsets.UTF_8.name()
            )
        }"
    }
}

fun createBiliHeader(): Map<String, String> {
    return mapOf(
        "user-agent" to
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36",
        "referer" to "https://space.bilibili.com/161775300/upload/video"
    )
}

fun md5(str: String): String {
    val md = MessageDigest.getInstance("MD5")
    val bytes = md.digest(str.toByteArray(StandardCharsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}