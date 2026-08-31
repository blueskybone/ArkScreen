package com.blueskybone.arkscreen.data.gacha

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 识别并解析 ArkScreen、小黑盒与官方压缩格式。
 *
 * 本类仅负责文件格式，不负责猜测缺失的卡池分类和干员 ID。
 */
class GachaImportDecoder(
    private val objectMapper: ObjectMapper,
    private val backupCodec: GachaBackupCodec,
) {

    fun decode(fileName: String?, content: String): GachaImportDocument {
        val trimmed = content.trim()
        require(trimmed.isNotEmpty()) { "导入文件为空" }
        if (!trimmed.startsWith("{")) return decodeArkScreen(trimmed)

        val root = runCatching { objectMapper.readTree(trimmed) }
            .getOrElse { throw IllegalArgumentException("导入文件不是有效的 JSON", it) }
        require(root.isObject) { "导入文件的 JSON 根节点必须是对象" }

        return when {
            root.has("version") -> decodeArkScreen(trimmed)
            root.path("info").isObject && root.path("data").isObject ->
                decodeXiaoheihe(fileName, root)
            looksLikeCompressedRecords(root) -> decodeOfficialCompressed(fileName, root)
            else -> throw IllegalArgumentException("无法识别寻访记录格式")
        }
    }

    private fun decodeArkScreen(content: String): GachaImportDocument {
        val payload = backupCodec.decode(content)
        return GachaImportDocument(
            format = GachaImportFormat.ARKSCREEN,
            sourceUid = payload.sourceUid,
            sourceName = payload.sourceName,
            records = payload.records.map { record ->
                RawGachaRecord(
                    timestamp = record.ts,
                    position = record.pos,
                    poolName = record.pool,
                    poolId = record.poolId,
                    poolCategory = record.poolCate,
                    charName = record.charName,
                    charId = record.charId,
                    rarity = record.rarity,
                    isNew = record.isNew,
                )
            },
        )
    }

    private fun decodeXiaoheihe(fileName: String?, root: JsonNode): GachaImportDocument {
        val info = root.path("info")
        val declaredUid = info.path("uid").asText().takeIf(String::isNotBlank)
        val fileUid = extractUid(fileName)
        val sourceUid = declaredUid ?: fileUid
        val warnings = commonMissingFieldWarnings().toMutableList()
        if (declaredUid == null && fileUid != null) {
            warnings += GachaImportWarning(
                GachaImportWarning.Code.SOURCE_UID_FROM_FILE_NAME,
                "账号 UID 来自文件名，请在导入前确认目标账号",
            )
        }
        return GachaImportDocument(
            format = GachaImportFormat.XIAOHEIHE,
            sourceUid = sourceUid,
            sourceName = info.path("export_app").asText().takeIf(String::isNotBlank),
            records = parseRecordGroups(root.path("data"), readPoolId = false),
            warnings = warnings,
        ).validated()
    }

    private fun decodeOfficialCompressed(fileName: String?, root: JsonNode): GachaImportDocument {
        val records = parseRecordGroups(root, readPoolId = true)
        val warnings = buildList {
            if (records.any { it.poolId == null }) add(
                GachaImportWarning(
                    GachaImportWarning.Code.MISSING_POOL_ID,
                    "部分记录缺少卡池 ID，将在导入前尝试通过资源表补全",
                )
            )
            add(GachaImportWarning(
                GachaImportWarning.Code.MISSING_POOL_CATEGORY,
                "文件未提供卡池分类，将在导入前通过资源表补全",
            ))
            add(GachaImportWarning(
                GachaImportWarning.Code.MISSING_CHAR_ID,
                "文件未提供干员 ID，将在导入前通过资源表补全",
            ))
        }
        val sourceUid = extractUid(fileName)
        return GachaImportDocument(
            format = GachaImportFormat.OFFICIAL_COMPRESSED,
            sourceUid = sourceUid,
            sourceName = null,
            records = records,
            warnings = warnings + if (sourceUid != null) listOf(
                GachaImportWarning(
                    GachaImportWarning.Code.SOURCE_UID_FROM_FILE_NAME,
                    "账号 UID 来自文件名，请在导入前确认目标账号",
                )
            ) else emptyList(),
        ).validated()
    }

    private fun parseRecordGroups(root: JsonNode, readPoolId: Boolean): List<RawGachaRecord> {
        require(root.isObject) { "寻访记录数据必须是对象" }
        return buildList {
            root.fields().forEach { (timestampText, group) ->
                val timestamp = parseTimestampMillis(timestampText)
                val poolName = group.path("p").asText()
                require(poolName.isNotBlank()) { "时间 $timestampText 的记录缺少卡池名称" }
                val poolId = if (readPoolId) {
                    group.path("pi").asText().takeIf(String::isNotBlank)
                } else null
                val characters = group.path("c")
                require(characters.isArray && characters.size() in 1..10) {
                    "时间 $timestampText 的干员列表格式错误"
                }
                characters.forEachIndexed { position, character ->
                    require(character.isArray && character.size() >= 3) {
                        "时间 $timestampText 的第 ${position + 1} 条记录格式错误"
                    }
                    val charName = character.path(0).asText()
                    require(charName.isNotBlank()) {
                        "时间 $timestampText 的第 ${position + 1} 条记录缺少干员名称"
                    }
                    val rarity = character.path(1).takeIf(JsonNode::isIntegralNumber)?.asInt()
                        ?: throw IllegalArgumentException(
                            "时间 $timestampText 的第 ${position + 1} 条记录稀有度格式错误"
                        )
                    val isNewValue = character.path(2).takeIf(JsonNode::isIntegralNumber)?.asInt()
                        ?: throw IllegalArgumentException(
                            "时间 $timestampText 的第 ${position + 1} 条记录新干员标记格式错误"
                        )
                    require(isNewValue == 0 || isNewValue == 1) {
                        "时间 $timestampText 的第 ${position + 1} 条记录新干员标记无效"
                    }
                    add(
                        RawGachaRecord(
                            timestamp = timestamp,
                            position = position,
                            poolName = poolName,
                            poolId = poolId,
                            charName = charName,
                            rarity = rarity,
                            isNew = isNewValue == 1,
                        )
                    )
                }
            }
        }
    }

    private fun GachaImportDocument.validated(): GachaImportDocument = apply {
        require(records.isNotEmpty()) { "导入文件中没有寻访记录" }
        require(records.size <= MAX_RECORDS) { "导入记录数量超过上限" }
        records.forEachIndexed { index, record ->
            require(record.rarity in 0..5) { "第 ${index + 1} 条记录的稀有度无效" }
            require(record.timestamp > 0) { "第 ${index + 1} 条记录的时间无效" }
        }
    }

    private fun looksLikeCompressedRecords(root: JsonNode): Boolean {
        if (root.isEmpty) return false
        return root.fields().asSequence().all { (timestamp, value) ->
            timestamp.toBigDecimalOrNull() != null && value.isObject && value.path("c").isArray
        }
    }

    private fun parseTimestampMillis(value: String): Long {
        val seconds = value.toBigDecimalOrNull()
            ?: throw IllegalArgumentException("记录时间格式错误：$value")
        return try {
            seconds.movePointRight(3).setScale(0, RoundingMode.DOWN).longValueExact()
        } catch (error: ArithmeticException) {
            throw IllegalArgumentException("记录时间超出支持范围：$value", error)
        }
    }

    private fun extractUid(fileName: String?): String? = fileName
        ?.let(UID_IN_FILE_NAME::find)
        ?.groupValues
        ?.get(1)

    private fun commonMissingFieldWarnings() = listOf(
        GachaImportWarning(
            GachaImportWarning.Code.MISSING_POOL_ID,
            "文件未提供卡池 ID，将在导入前通过资源表补全",
        ),
        GachaImportWarning(
            GachaImportWarning.Code.MISSING_POOL_CATEGORY,
            "文件未提供卡池分类，将在导入前通过资源表补全",
        ),
        GachaImportWarning(
            GachaImportWarning.Code.MISSING_CHAR_ID,
            "文件未提供干员 ID，将在导入前通过资源表补全",
        ),
    )

    private companion object {
        const val MAX_RECORDS = 100_000
        val UID_IN_FILE_NAME = Regex("(?:^|_)(\\d{6,12})(?:_|\\.|$)")
    }
}
