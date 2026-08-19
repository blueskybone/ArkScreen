package com.blueskybone.arkscreen.data.gacha

/** 导入文件的来源格式。 */
enum class GachaImportFormat {
    ARKSCREEN,
    XIAOHEIHE,
    OFFICIAL_COMPRESSED,
}

/**
 * 尚未经过本地资源补全的寻访记录。
 *
 * 第三方文件可能不包含卡池 ID、卡池分类或干员 ID，因此这些字段必须允许为空；
 * 后续由独立的资源解析阶段补全，避免格式解析器臆造官方标识。
 */
data class RawGachaRecord(
    val timestamp: Long,
    val position: Int,
    val poolName: String,
    val poolId: String? = null,
    val poolCategory: String? = null,
    val charName: String,
    val charId: String? = null,
    val rarity: Int,
    val isNew: Boolean,
)

data class GachaImportWarning(
    val code: Code,
    val message: String,
) {
    enum class Code {
        SOURCE_UID_FROM_FILE_NAME,
        MISSING_POOL_ID,
        MISSING_POOL_CATEGORY,
        MISSING_CHAR_ID,
        UNKNOWN_POOL,
        UNMATCHED_OPERATOR,
        AMBIGUOUS_OPERATOR,
    }
}

/** 统一格式解析结果；此时尚未转换成数据库可写入的完整 Record。 */
data class GachaImportDocument(
    val format: GachaImportFormat,
    val sourceUid: String?,
    val sourceName: String?,
    val records: List<RawGachaRecord>,
    val warnings: List<GachaImportWarning> = emptyList(),
)
