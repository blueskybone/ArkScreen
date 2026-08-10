package com.blueskybone.arkscreen.util

import android.net.Uri
import android.webkit.CookieManager
import timber.log.Timber

fun getCookie(url: String, name: String): String? {
    val cookie = CookieManager.getInstance().getCookie(url) ?: return null
    val rawValue = cookie.split(";")
        .asSequence()
        .map(String::trim)
        .firstOrNull { it.startsWith("$name=") }
        ?.substringAfter("=")
        ?: return null

    return try {
        // Cookie 值不是表单数据，将“+”转换为空格会破坏令牌内容。
        Uri.decode(rawValue)
    } catch (error: Exception) {
        Timber.e(error, "Failed to decode cookie value")
        rawValue
    }
}
