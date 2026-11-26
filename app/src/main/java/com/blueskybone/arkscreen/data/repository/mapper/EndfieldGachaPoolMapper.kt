package com.blueskybone.arkscreen.data.repository.mapper

import com.blueskybone.arkscreen.data.local.room.EndfieldGachaPool

internal fun EndfieldGachaPool.toDomain() = com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaPool(
    roleId = roleId,
    type = type,
    poolId = poolId,
    poolName = poolName,
    up6Ids = up6Ids,
    officialCount = officialCount,
)
