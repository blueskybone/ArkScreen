package com.blueskybone.arkscreen.ui.gacha.model

import com.blueskybone.arkscreen.domain.model.gacha.Record
import org.junit.Assert.assertEquals
import org.junit.Test

class GachaUiMapperTest {

    @Test
    fun `records in the same ten-pull keep descending position order and both counts`() {
        val timestamp = 1_750_000_000_000L
        val records = (0..2).map { position ->
            Record(
                id = position.toString(),
                pool = "测试卡池",
                poolId = "pool",
                poolCate = "NORMAL",
                charName = "干员$position",
                charId = "char_$position",
                rarity = 3,
                isNew = false,
                pos = position,
                ts = timestamp,
            )
        }

        val mapped = GachaUiMapper.map(records).records

        assertEquals(listOf(2, 1, 0), mapped.map { it.pos })
        assertEquals(listOf(3, 2, 1), mapped.map { it.count })
        assertEquals(listOf(3, 2, 1), mapped.map { it.gachaCount })
    }
}
