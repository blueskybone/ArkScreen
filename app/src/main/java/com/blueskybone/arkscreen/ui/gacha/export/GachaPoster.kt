package com.blueskybone.arkscreen.ui.gacha.export

import com.blueskybone.arkscreen.ui.gacha.GachaUiState
import com.blueskybone.arkscreen.ui.gacha.model.GachaPool

/** Immutable export snapshot; neither collapsed rows nor record-only filters truncate pool history. */
data class GachaPoster(
    val nickname: String,
    val official: Boolean,
    val pools: List<GachaPool>,
    val firstTimestamp: Long,
    val lastTimestamp: Long,
) {
    val visiblePools: List<GachaPool> get() = pools.filter { it.hitRecords.isNotEmpty() }
    val total: Int get() = pools.sumOf { it.totalCount }
    val sixStars: Int get() = pools.sumOf { it.hitRecords.size }
    val average: Double? get() = if (sixStars == 0) null else total.toDouble() / sixStars

    companion object {
        fun from(state: GachaUiState): GachaPoster? {
            val account = state.currAccount ?: return null
            val snapshot = state.gachaUiSnapshot ?: return null
            val pools = snapshot.gachaPools.filter {
                state.selectedPoolId == "ALL" || it.poolId == state.selectedPoolId
            }
            val ids = pools.map { it.poolId }.toSet()
            val records = snapshot.records.filter { it.poolId in ids }
            if (records.isEmpty()) return null
            return GachaPoster(account.nickName, account.official, pools,
                records.minOf { it.ts }, records.maxOf { it.ts })
        }
    }
}
