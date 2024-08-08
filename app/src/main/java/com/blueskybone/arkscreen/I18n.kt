package com.blueskybone.arkscreen

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.android.ext.android.getKoin
import org.koin.java.KoinJavaComponent.getKoin
import java.io.FileInputStream

/**
 *   Created by blueskybone
 *   Date: 2024/7/26
 */
class I18n {
    enum class ConvertType(val printableName: String) {
        Recruit("recruit"),
        Profession("profession"),
        SubProfession("sub_profession")
    }

    private var node: JsonNode

    init {
        val updateResource: UpdateResource by getKoin().inject()
        val filePath: String = updateResource.getResourceFilepath(UpdateResource.Resource.I18n)
        val inputStream = FileInputStream(filePath)
        val om = ObjectMapper()
        node = om.readTree(inputStream)
    }

    fun convert(code: String, type: ConvertType): String {
        return try {
            val subNode = node[type.printableName]
            subNode.get(code).asText()
        } catch (e: Exception) {
            code
        }
    }
}