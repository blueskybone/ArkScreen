package com.blueskybone.arkscreen.data.license

import android.content.Context
import com.blueskybone.arkscreen.domain.model.license.OpenSourceLibrary
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * 读取 Gradle 构建期间由 Maven POM 文件生成的依赖许可元数据。
 */
class GeneratedLicenseCatalog(
    private val context: Context,
    private val objectMapper: ObjectMapper = ObjectMapper(),
) {
    fun load(): List<OpenSourceLibrary> {
        return context.assets.open(ASSET_NAME).use { input ->
            objectMapper.readValue(
                input,
                object : TypeReference<List<OpenSourceLibrary>>() {},
            )
        }
    }

    private companion object {
        const val ASSET_NAME = "third_party_licenses.json"
    }
}
