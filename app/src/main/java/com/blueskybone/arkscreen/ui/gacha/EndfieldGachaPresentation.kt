package com.blueskybone.arkscreen.ui.gacha

import com.blueskybone.arkscreen.domain.model.gacha.EndfieldRecordProgress
import com.blueskybone.arkscreen.domain.usecase.gacha.calculateEndfieldRecordProgress
import com.blueskybone.arkscreen.domain.model.gacha.EndfieldPoolProgress
import com.blueskybone.arkscreen.domain.usecase.gacha.EndfieldPityCalculator
import com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaRecord
import com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaPool

internal data class EndfieldGachaInput(
    val uid: String?, val type: String,
    val records: List<EndfieldGachaRecord>, val pools: List<EndfieldGachaPool>,
)

internal data class EndfieldRecordFilter(
    val scope: String? = null, val pool: String? = null,
    val sixOnly: Boolean = false, val newOnly: Boolean = false,
)

internal data class EndfieldGachaPresentation(
    val input: EndfieldGachaInput,
    val records: List<EndfieldGachaRecord>,
    val pools: List<EndfieldPoolProgress>,
    val choices: List<Pair<String, String>>,
    val rows: List<EndfieldRecordProgress>,
    val six: Int, val five: Int, val free: Int,
    val average: Double?, val best: Int?, val worst: Int?,
) {
    val scope: String get() = "${input.uid}:${input.type}"
    fun selectedPool(filter: EndfieldRecordFilter): String? = filter.pool
        ?.takeIf { filter.scope == scope && choices.any { choice -> choice.first == it } }
    fun filterRows(filter: EndfieldRecordFilter): List<EndfieldRecordProgress> {
        val pool = selectedPool(filter)
        return rows.filter { row ->
            (pool == null || row.record.poolId == pool) &&
                (!filter.sixOnly || row.record.rarity == 6) && (!filter.newOnly || row.record.isNew)
        }
    }
}

internal data class EndfieldGachaPageData(
    val summary: EndfieldGachaPresentation,
    val filter: EndfieldRecordFilter,
    val rows: List<EndfieldRecordProgress>,
)

internal fun buildEndfieldGachaPresentation(input: EndfieldGachaInput): EndfieldGachaPresentation {
    val records = input.records.filter { it.type == input.type && !EndfieldPityCalculator.isGift(it) }
        .sortedWith(compareByDescending<EndfieldGachaRecord> { it.gachaTs.toLongOrNull() ?: 0L }
            .thenByDescending { it.seqId.length }.thenByDescending { it.seqId })
    val pools = EndfieldPityCalculator.calculate(input.records, input.pools, input.type)
    val hits = pools.flatMap { it.hits }.filterNot { it.isFree }
    val six = records.count { it.rarity == 6 }
    // Match the displayed cumulative count and six-star count, including free draws.
    val average = if (six == 0) null else records.size.toDouble() / six
    return EndfieldGachaPresentation(
        input, records, pools,
        records.distinctBy { it.poolId }.map { it.poolId to it.poolName.ifBlank { it.poolId } },
        calculateEndfieldRecordProgress(records), six, records.count { it.rarity == 5 },
        records.count { it.isFree }, average,
        hits.minOfOrNull { it.pity }, hits.maxOfOrNull { it.pity },
    )
}
