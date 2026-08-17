package com.blueskybone.arkscreen.domain.service

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
interface TextTranslator {
    suspend fun translate(key: String, fallback: String = key): String

    suspend fun translateAll(keys: Collection<String>): Map<String, String>
}
