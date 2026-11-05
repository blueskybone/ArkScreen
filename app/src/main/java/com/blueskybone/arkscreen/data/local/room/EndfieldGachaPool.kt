package com.blueskybone.arkscreen.data.local.room

import androidx.room.Entity

@Entity(primaryKeys = ["roleId", "type", "poolId"])
data class EndfieldGachaPool(
    val roleId: String,
    val type: String,
    val poolId: String,
    val poolName: String,
    val up6Ids: String,
    val officialCount: Int? = null,
)
