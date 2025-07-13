package com.blueskybone.arkscreen.network.model

data class GachaResponse(
    val code: Int,
    val data: GachaData
)

data class GachaData(
    val recordList: List<Record>
)

data class Record(
    val ts: Long,
    val pool: String,
    val charsList: List<Chars>
)

data class Chars(
    val name: String,
    val rarity: Int,
    val isNew: Boolean
)