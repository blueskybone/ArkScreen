package com.blueskybone.arkscreen.data.gacha

import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.gacha.GachaPoolCatalogEntry
import com.blueskybone.arkscreen.domain.model.gacha.Record
import com.blueskybone.arkscreen.domain.model.operator.OperatorBasicInfo
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import kotlinx.coroutines.flow.lastOrNull
import java.security.MessageDigest

data class ResolvedGachaImport(
    val payload: GachaImportPayload,
    val warnings: List<GachaImportWarning>,
)

/** 使用可更新资源将第三方原始记录补全为数据库可写入的记录。 */
class GachaImportResolver(
    private val resources: GameResourceRepository,
) {

    suspend fun resolve(document: GachaImportDocument): Result<ResolvedGachaImport> = runCatching {
        if (document.records.all(::isCompleteRecord)) {
            return@runCatching resolveCompleteDocument(document)
        }

        // 同步失败时仓库仍会回退到最近一次有效缓存或安装包内置资源。
        resources.syncResource(ConfigType.CHAR_MAP).lastOrNull()
        resources.syncResource(ConfigType.GACHA_POOL_CATALOG).lastOrNull()

        val operators = resources.getOperatorBasicInfoMap().getOrThrow()
        val catalog = resources.getGachaPoolCatalog().getOrThrow().entries
        val operatorsByName = operators.values.groupBy { normalizeName(it.name) }
        val warnings = document.warnings
            .filterNot { it.code in RESOLVED_MISSING_FIELD_CODES }
            .toMutableList()
        val warnedCodes = mutableSetOf<Pair<GachaImportWarning.Code, String>>()

        val records = document.records.map { raw ->
            val pool = resolvePool(raw, catalog)
            if (pool.unknown) {
                warnings.addOnce(
                    warnedCodes,
                    GachaImportWarning.Code.UNKNOWN_POOL,
                    raw.poolName,
                    "未在卡池对照表中找到“${raw.poolName}”，已按未知卡池导入",
                )
            }

            val operator = resolveOperator(raw, operators, operatorsByName)
            if (operator.candidates > 1) {
                warnings.addOnce(
                    warnedCodes,
                    GachaImportWarning.Code.AMBIGUOUS_OPERATOR,
                    raw.charName,
                    "干员“${raw.charName}”对应多个 ID，已按稀有度和基础形态选择",
                )
            }
            if (operator.info == null) {
                warnings.addOnce(
                    warnedCodes,
                    GachaImportWarning.Code.UNMATCHED_OPERATOR,
                    raw.charName,
                    "未找到干员“${raw.charName}”的 ID，已使用稳定占位 ID",
                )
            }

            Record(
                id = "${raw.timestamp}_${raw.position}",
                pool = raw.poolName,
                poolId = pool.poolId,
                poolCate = pool.category,
                charName = raw.charName,
                charId = raw.charId
                    ?: operator.info?.charId
                    ?: "UNKNOWN:${stableHash(normalizeName(raw.charName))}",
                rarity = raw.rarity,
                isNew = raw.isNew,
                pos = raw.position,
                ts = raw.timestamp,
            )
        }

        ResolvedGachaImport(
            payload = GachaImportPayload(
                sourceUid = document.sourceUid,
                sourceName = document.sourceName,
                records = records,
                warnings = warnings.distinctBy { it.code to it.message },
            ),
            warnings = warnings.distinctBy { it.code to it.message },
        )
    }

    private fun isCompleteRecord(record: RawGachaRecord): Boolean =
        !record.poolId.isNullOrBlank() &&
            !record.poolCategory.isNullOrBlank() &&
            !record.charId.isNullOrBlank()

    /** ArkScreen 自有备份信息完整，不应为了导入而额外依赖远端资源。 */
    private fun resolveCompleteDocument(document: GachaImportDocument): ResolvedGachaImport {
        val records = document.records.map { raw ->
            Record(
                id = "${raw.timestamp}_${raw.position}",
                pool = raw.poolName,
                poolId = requireNotNull(raw.poolId),
                poolCate = requireNotNull(raw.poolCategory),
                charName = raw.charName,
                charId = requireNotNull(raw.charId),
                rarity = raw.rarity,
                isNew = raw.isNew,
                pos = raw.position,
                ts = raw.timestamp,
            )
        }
        val payload = GachaImportPayload(
            sourceUid = document.sourceUid,
            sourceName = document.sourceName,
            records = records,
            warnings = document.warnings,
        )
        return ResolvedGachaImport(payload, document.warnings)
    }

    private fun resolvePool(
        raw: RawGachaRecord,
        entries: List<GachaPoolCatalogEntry>,
    ): PoolResolution {
        val explicitId = raw.poolId
        val byId = explicitId?.let { id -> entries.firstOrNull { it.poolId == id } }
        val byNameAndTime = entries
            .asSequence()
            .filter { entry -> entry.names.any { normalizeName(it) == normalizeName(raw.poolName) } }
            .filter { entry ->
                (entry.startTs == null || raw.timestamp >= entry.startTs) &&
                    (entry.endTs == null || raw.timestamp <= entry.endTs)
            }
            // 重叠时优先使用时间范围更窄、信息更精确的条目。
            .minByOrNull { entry ->
                if (entry.startTs != null && entry.endTs != null) {
                    entry.endTs - entry.startTs
                } else Long.MAX_VALUE
            }
        val match = byId ?: byNameAndTime
        val category = raw.poolCategory
            ?: match?.category
            ?: categoryFromOfficialId(explicitId)
        val resolvedId = explicitId
            ?: match?.poolId
            ?: match?.let { "LEGACY:${it.id}" }
            ?: "LEGACY:UNKNOWN:${stableHash(normalizeName(raw.poolName))}"
        return PoolResolution(
            poolId = resolvedId,
            category = category ?: "UN",
            unknown = match == null && explicitId == null,
        )
    }

    private fun resolveOperator(
        raw: RawGachaRecord,
        operators: Map<String, OperatorBasicInfo>,
        operatorsByName: Map<String, List<OperatorBasicInfo>>,
    ): OperatorResolution {
        raw.charId?.let { return OperatorResolution(operators[it], if (it in operators) 1 else 0) }
        val candidates = operatorsByName[normalizeName(raw.charName)].orEmpty()
        val rarityMatched = candidates.filter { it.rarity == raw.rarity }.ifEmpty { candidates }
        // 同名职业转换形态通常不可从卡池获得；优先选择编号更早的基础形态。
        val selected = rarityMatched.minWithOrNull(
            compareBy<OperatorBasicInfo> { isAlternateForm(it.charId) }.thenBy { it.charId }
        )
        return OperatorResolution(selected, rarityMatched.size)
    }

    private fun categoryFromOfficialId(poolId: String?): String? = when {
        poolId == null -> null
        poolId.startsWith("LINKAGE") -> "LINKAGE"
        poolId.startsWith("LIMITED") || poolId.startsWith("ATTAIN") -> "LIMITED"
        poolId.startsWith("CLASSIC") || poolId.startsWith("FESCLASSIC") -> "CLASSIC"
        poolId.startsWith("SINGLE") || poolId.startsWith("DOUBLE") ||
            poolId.startsWith("SPECIAL") || poolId.startsWith("NORM") -> "NORMAL"
        else -> null
    }

    private fun normalizeName(value: String): String = value
        .trim()
        .replace('·', '・')
        .replace(Regex("\\s+"), "")

    private fun isAlternateForm(charId: String): Boolean =
        Regex("^char_1\\d{3}_").containsMatchIn(charId)

    private fun stableHash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .take(8)
        .joinToString("") { "%02x".format(it) }

    private fun MutableList<GachaImportWarning>.addOnce(
        seen: MutableSet<Pair<GachaImportWarning.Code, String>>,
        code: GachaImportWarning.Code,
        subject: String,
        message: String,
    ) {
        if (seen.add(code to subject)) add(GachaImportWarning(code, message))
    }

    private data class PoolResolution(
        val poolId: String,
        val category: String,
        val unknown: Boolean,
    )

    private data class OperatorResolution(
        val info: OperatorBasicInfo?,
        val candidates: Int,
    )

    private companion object {
        val RESOLVED_MISSING_FIELD_CODES = setOf(
            GachaImportWarning.Code.MISSING_POOL_ID,
            GachaImportWarning.Code.MISSING_POOL_CATEGORY,
            GachaImportWarning.Code.MISSING_CHAR_ID,
        )
    }
}
