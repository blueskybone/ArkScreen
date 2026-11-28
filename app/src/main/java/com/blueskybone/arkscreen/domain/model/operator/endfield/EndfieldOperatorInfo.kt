package com.blueskybone.arkscreen.domain.model.operator.endfield

/** Wiki directory metadata; separate from an account's cultivation data. */
data class EndfieldOperatorInfo(
    val wikiId: String,
    val charId: String?,
    val name: String,
    val coverUrl: String,
    val rarity: Int,
    val profession: String,
    val property: String,
    val isPreview: Boolean,
)
