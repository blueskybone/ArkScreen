package com.blueskybone.arkscreen.data.repository.mapper

import com.blueskybone.arkscreen.data.local.room.EndfieldGachaRecord

internal fun EndfieldGachaRecord.toDomain() = com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaRecord(
    roleId = roleId,
    type = type,
    seqId = seqId,
    poolId = poolId,
    poolName = poolName,
    poolType = poolType,
    itemId = itemId,
    itemName = itemName,
    rarity = rarity,
    gachaTs = gachaTs,
    kind = kind,
    isNew = isNew,
    isFree = isFree,
    poolVersion = poolVersion,
)
