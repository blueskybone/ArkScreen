package com.blueskybone.arkscreen.domain.model.link

import java.net.URI

/**
 * Validates user links before they reach persistence or a browser.
 */
object LinkUrl {
    private val allowedSchemes = setOf("http", "https")

    fun normalize(value: String): Result<String> = runCatching {
        val uri = URI(value.trim())
        require(uri.scheme?.lowercase() in allowedSchemes && !uri.host.isNullOrBlank()) {
            "请输入有效的 HTTP 或 HTTPS 网址"
        }
        uri.normalize().toASCIIString()
    }
}
