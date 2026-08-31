package com.blueskybone.arkscreen.data.resource

import com.blueskybone.arkscreen.domain.model.gacha.GachaPoolCatalog
import com.blueskybone.arkscreen.domain.model.gacha.GachaPoolCatalogEntry
import com.fasterxml.jackson.databind.JsonNode
import java.io.File

class GachaPoolCatalogParser(
    private val jsonReader: ResourceJsonReader,
) {

    fun parse(file: File): GachaPoolCatalog = parse(jsonReader.readNode(file))

    internal fun parse(root: JsonNode): GachaPoolCatalog {
        val pools = root["pools"] ?: error("gacha_pool_catalog file missing pools")
        require(pools.isArray) { "gacha_pool_catalog pools must be an array" }

        val entries = pools.mapIndexed { index, node ->
            val id = node["id"]?.asText().orEmpty().trim()
            val category = node["category"]?.asText().orEmpty().trim()
            val names = node["names"]
                ?.takeIf { it.isArray }
                ?.map { it.asText().trim() }
                ?.filter(String::isNotBlank)
                ?.toSet()
                .orEmpty()
            require(id.isNotBlank()) { "第 ${index + 1} 个卡池缺少 id" }
            require(names.isNotEmpty()) { "卡池 $id 缺少名称" }
            require(category in VALID_CATEGORIES) { "卡池 $id 的分类无效" }

            GachaPoolCatalogEntry(
                id = id,
                names = names,
                category = category,
                poolId = node["poolId"]?.asText()?.trim()?.takeIf(String::isNotBlank),
                startTs = node["startTs"]?.takeIf { it.isIntegralNumber }?.asLong(),
                endTs = node["endTs"]?.takeIf { it.isIntegralNumber }?.asLong(),
            ).also { entry ->
                require(entry.startTs == null || entry.endTs == null || entry.startTs <= entry.endTs) {
                    "卡池 $id 的时间范围无效"
                }
            }
        }
        require(entries.map(GachaPoolCatalogEntry::id).distinct().size == entries.size) {
            "gacha_pool_catalog 包含重复 id"
        }
        return GachaPoolCatalog(entries)
    }

    private companion object {
        val VALID_CATEGORIES = setOf("NORMAL", "LIMITED", "LINKAGE", "CLASSIC", "UN")
    }
}
