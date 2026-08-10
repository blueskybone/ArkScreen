package com.blueskybone.arkscreen.data.repository.mapper

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */

private val objectMapper = ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

/**
 * 这是一个通用的抽象函数：从 File 读取并解析为指定的 POJO
 */
suspend fun <T> File.readAsEntity(clazz: Class<T>): T = withContext(Dispatchers.IO) {
    try {
        this@readAsEntity.inputStream().use { input ->
            objectMapper.readValue(input, clazz)
        }
    } catch (e: Exception) {
        Timber.e(e, "JSON parse error for file: ${this@readAsEntity.name}")
        throw e // 或者返回一个预定义的默认对象
    }
}