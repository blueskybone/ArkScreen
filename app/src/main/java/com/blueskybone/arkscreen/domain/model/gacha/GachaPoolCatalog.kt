package com.blueskybone.arkscreen.domain.model.gacha

data class GachaPoolCatalog(
    val entries: List<GachaPoolCatalogEntry>,
)

/**
 * 第三方记录使用的卡池对照项。
 *
 * [id] 是资源表内部的稳定标识；[poolId] 仅在能够确认官方 ID 时填写。
 * 同名卡池可以通过不同时间范围拆成多项，避免旧格式记录被错误合并。
 */
data class GachaPoolCatalogEntry(
    val id: String,
    val names: Set<String>,
    val category: String,
    val poolId: String?,
    val startTs: Long?,
    val endTs: Long?,
)
