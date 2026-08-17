package com.blueskybone.arkscreen.ui.gacha.model

data class GachaUiSnapshot(
    val records: List<Record>,
    val gachaPoolStats: List<GachaPoolStats>,
    val gachaSummary: GachaSummary,
    val gachaPools: List<GachaPool> = emptyList(),
    val gachaOverview: GachaOverview
)