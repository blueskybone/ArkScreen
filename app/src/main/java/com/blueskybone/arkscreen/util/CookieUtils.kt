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
        // Cookie values are not form data; converting '+' to a space corrupts tokens.
        Uri.decode(rawValue)
    } catch (error: Exception) {
        Timber.e(error, "Failed to decode cookie value")
        rawValue
    }
}
