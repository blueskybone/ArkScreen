package com.blueskybone.arkscreen.data.gacha

import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.domain.model.gacha.Record
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class GachaImportPayload(
    val sourceUid: String?,
    val sourceName: String?,
    val records: List<Record>,
)

class GachaBackupCodec(private val objectMapper: ObjectMapper) {

    fun encodeJson(account: AccountGc, records: List<Record>): String =
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
            ExportBundle(
                sourceUid = account.uid,
                sourceName = account.nickName,
                exportedAt = System.currentTimeMillis(),
                records = records,
            )
        )

    fun encodeText(account: AccountGc, records: List<Record>): String = buildString {
        appendLine("# ArkScreen Gacha Export v1")
        appendLine("# sourceUid=${account.uid.cleanTsv()}")
        appendLine("# sourceName=${account.nickName.cleanTsv()}")
        appendLine("pool\tpoolId\tpoolCate\tcharName\tcharId\trarity\tisNew\tpos\tts")
        records.forEach { record ->
            appendLine(
                listOf(
                    record.pool.cleanTsv(),
                    record.poolId.cleanTsv(),
                    record.poolCate.cleanTsv(),
                    record.charName.cleanTsv(),
                    record.charId.cleanTsv(),
                    record.rarity,
                    record.isNew,
                    record.pos,
                    record.ts,
                ).joinToString("\t")
            )
        }
    }

    fun decode(content: String): GachaImportPayload {
        val trimmed = content.trim()
        require(trimmed.isNotEmpty()) { "导入文件为空" }
        val payload = if (trimmed.startsWith("{")) decodeJson(trimmed) else decodeText(trimmed)
        validateRecords(payload.records)
        return payload
    }

    private fun decodeJson(content: String): GachaImportPayload {
        val bundle: ExportBundle = objectMapper.readValue(content)
        require(bundle.version == CURRENT_VERSION) { "暂不支持此备份文件版本" }
        return GachaImportPayload(
            sourceUid = bundle.sourceUid.takeIf(String::isNotBlank),
            sourceName = bundle.sourceName.takeIf(String::isNotBlank),
            records = bundle.records,
        )
    }

    private fun decodeText(content: String): GachaImportPayload {
        val lines = content.lineSequence().toList()
        val records = lines
            .filterNot { it.isBlank() || it.startsWith("#") || it.startsWith("pool\t") }
            .mapIndexed { index, line ->
                val fields = line.split('\t')
                val row = index + 1
                require(fields.size == FIELD_COUNT) { "第 $row 条记录格式错误" }
                Record(
                    id = "${fields[8]}_${fields[7]}",
                    pool = fields[0],
                    poolId = fields[1],
                    poolCate = fields[2],
                    charName = fields[3],
                    charId = fields[4],
                    rarity = fields[5].toIntOrNull()
                        ?: error("第 $row 条记录的稀有度格式错误"),
                    isNew = fields[6].toBooleanStrictOrNull()
                        ?: error("第 $row 条记录的新干员标记格式错误"),
                    pos = fields[7].toIntOrNull()
                        ?: error("第 $row 条记录的位置格式错误"),
                    ts = fields[8].toLongOrNull()
                        ?: error("第 $row 条记录的时间格式错误"),
                )
            }
            .toList()
        return GachaImportPayload(
            sourceUid = lines.readMetadata("sourceUid"),
            sourceName = lines.readMetadata("sourceName"),
            records = records,
        )
    }

    private fun validateRecords(records: List<Record>) {
        require(records.isNotEmpty()) { "导入文件中没有寻访记录" }
        require(records.size <= MAX_RECORDS) { "导入记录数量超过上限" }
        records.forEachIndexed { index, record ->
            val row = index + 1
            require(record.pool.isNotBlank()) { "第 $row 条记录缺少卡池名称" }
            require(record.poolId.isNotBlank()) { "第 $row 条记录缺少卡池 ID" }
            require(record.charName.isNotBlank()) { "第 $row 条记录缺少干员名称" }
            require(record.charId.isNotBlank()) { "第 $row 条记录缺少干员 ID" }
            require(record.poolCate in VALID_POOL_CATEGORIES) {
                "第 $row 条记录的卡池分类无效"
            }
            require(record.rarity in 0..5) { "第 $row 条记录的稀有度无效" }
            require(record.pos >= 0) { "第 $row 条记录的位置无效" }
            require(record.ts > 0) { "第 $row 条记录的时间无效" }
        }
    }

    private fun List<String>.readMetadata(key: String): String? {
        val prefix = "# $key="
        return firstOrNull { it.startsWith(prefix) }
            ?.removePrefix(prefix)
            ?.trim()
            ?.takeIf(String::isNotBlank)
    }

    private fun String.cleanTsv(): String =
        replace('\t', ' ').replace('\n', ' ').replace('\r', ' ')

    private data class ExportBundle(
        val version: Int = CURRENT_VERSION,
        val sourceUid: String = "",
        val sourceName: String = "",
        val exportedAt: Long = 0L,
        val records: List<Record> = emptyList(),
    )

    private companion object {
        const val CURRENT_VERSION = 1
        const val FIELD_COUNT = 9
        const val MAX_RECORDS = 100_000
        val VALID_POOL_CATEGORIES = setOf("NORMAL", "LIMITED", "LINKAGE", "CLASSIC", "UN")
    }
}
