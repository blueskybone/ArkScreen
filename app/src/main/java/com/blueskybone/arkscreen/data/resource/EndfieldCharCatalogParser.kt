package com.blueskybone.arkscreen.data.resource

import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldOperatorInfo
import com.fasterxml.jackson.databind.JsonNode

object EndfieldCharCatalogParser {
    fun parse(root: JsonNode): List<EndfieldOperatorInfo> {
        require(root.path("update").path("version").asInt() > 0) { "Invalid Endfield catalog version" }
        val entries = root.path("operators")
        require(entries.isArray && !entries.isEmpty) { "Missing Endfield operators" }
        val result = entries.map { node ->
            fun text(key: String): String {
                val value = node.path(key)
                require(value.isTextual && value.asText().isNotBlank()) { "Invalid Endfield field: $key" }
                return value.asText()
            }
            val charId = node.path("charId")
            require(charId.isNull || (charId.isTextual && charId.asText().isNotBlank())) { "Invalid charId" }
            val rarity = node.path("rarity")
            require(rarity.isIntegralNumber && rarity.asInt() in 1..6) { "Invalid Endfield rarity" }
            EndfieldOperatorInfo(
                wikiId = text("wikiId").also { require(it.all(Char::isDigit)) },
                charId = charId.takeUnless { it.isNull }?.asText(),
                name = text("name"), coverUrl = text("coverUrl"), rarity = rarity.asInt(),
                profession = text("profession"), property = text("property"),
                isPreview = node.path("marker").asText() == "label_type_preview",
            )
        }
        require(result.map { it.wikiId }.distinct().size == result.size) { "Duplicate Wiki IDs" }
        val ids = result.mapNotNull { it.charId }
        require(ids.distinct().size == ids.size) { "Duplicate Endfield character IDs" }
        return result
    }
}
