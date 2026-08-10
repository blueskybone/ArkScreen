package com.blueskybone.arkscreen.legacy

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.FileInputStream

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */


/**
 * 重构结束后删除
 * */
@Deprecated("重构结束后删除")
class I18nManager private constructor(){
    //TODO：换成新的i18n_new.json的逻辑，取消TYPE
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