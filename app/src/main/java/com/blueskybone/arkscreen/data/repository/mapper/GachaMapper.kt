package com.blueskybone.arkscreen.data.repository.mapper

import com.blueskybone.arkscreen.data.local.room.Gacha
import com.blueskybone.arkscreen.domain.model.gacha.Record

object GachaMapper {

    fun toEntity(uid: String, record: Record): Gacha {
        return Gacha(
            poolId = record.poolId,
            poolCate = record.poolCate,
            pool = record.pool,
            uid = uid,
            ts = record.ts,
            charName = record.charName,
            charId = record.poolId,
            isNew = record.isNew,
            rarity = record.rarity
        )
    }

    fun toDomain(gacha: Gacha): Record{
        return Record(
            id = gacha.id.toString(),
            poolId = gacha.poolId,
            poolCate = gacha.poolCate,
            pool = gacha.pool,
            ts = gacha.ts,
            charName = gacha.charName,
            charId = gacha.poolId,
            isNew = gacha.isNew,
            rarity = gacha.rarity,
            pos = gacha.pos
        )
    }

}