package com.blueskybone.arkscreen.domain.usecase.gacha

import com.blueskybone.arkscreen.domain.model.gacha.EndfieldRecordProgress
import com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaRecord

/** Input is newest-first; count before applying display filters. */
fun calculateEndfieldRecordProgress(records: List<EndfieldGachaRecord>): List<EndfieldRecordProgress> {
    val totals = mutableMapOf<String, Int>()
    val pity = mutableMapOf<String, Int>()
    val versions = mutableMapOf<String, Int>()
    return records.asReversed().map { record ->
        val key = record.poolId.ifBlank { record.poolName.ifBlank { record.poolType } }
        if (record.type == "weapon" && (record.poolType == "rerun" || record.poolId.startsWith("rerun", true))) {
            record.poolVersion?.let { version ->
                if (versions[key] != null && versions[key] != version) pity[key] = 0
                versions[key] = version
            }
        }
        val total = (totals[key] ?: 0) + 1
        totals[key] = total
        val count = if (record.isFree) 0 else (pity[key] ?: 0) + 1
        if (!record.isFree) pity[key] = if (record.rarity == 6) 0 else count
        EndfieldRecordProgress(record, count, total)
    }.asReversed()
}
