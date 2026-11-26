package com.blueskybone.arkscreen.domain.model.gacha

data class EndfieldPoolProgress(
    val key: String,
    val name: String,
    val total: Int,
    val free: Int,
    val six: Int,
    val five: Int,
    val smallCount: Int,
    val smallMax: Int?,
    val bigCount: Int?,
    val bigMax: Int?,
    val bigSatisfied: Boolean,
    val bigUsesOfficialCount: Boolean,
    val hits: List<EndfieldSixHit>,
)

data class EndfieldSixHit(
    val seqId: String,
    val name: String,
    val pity: Int,
    val isFree: Boolean,
    val isNew: Boolean,
    val gachaTs: String,
)
