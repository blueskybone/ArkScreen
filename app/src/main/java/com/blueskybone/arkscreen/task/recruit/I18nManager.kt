package com.blueskybone.arkscreen.task.recruit

import com.blueskybone.arkscreen.I18n
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.FileInputStream

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */
class I18nManager private constructor(){

    private object Holder {
        val INSTANCE = I18nManager()
    }

    companion object {
        val instance: I18nManager by lazy { Holder.INSTANCE }
    }

    enum class ConvertType(val printableName: String) {
        Recruit("recruit"),
        Profession("profession"),
        SubProfession("sub_profession")
    }

    private var node: JsonNode

    init {
        val inputStream = FileInputStream(I18n.getFilePath())
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