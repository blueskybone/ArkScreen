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

    @Test
    fun `linkage records contribute to the limited pool pity count`() {
        val records = listOf(
            Record(
                id = "1", pool = "限定池", poolId = "LIMITED_1", poolCate = "LIMITED",
                charName = "干员1", charId = "char_1", rarity = 3, isNew = false, pos = 0, ts = 1000L,
            ),
            Record(
                id = "2", pool = "联动池", poolId = "LINKAGE_1", poolCate = "LINKAGE",
                charName = "干员2", charId = "char_2", rarity = 3, isNew = false, pos = 0, ts = 2000L,
            ),
            Record(
                id = "3", pool = "联动池", poolId = "LINKAGE_1", poolCate = "LINKAGE",
                charName = "干员3", charId = "char_3", rarity = 3, isNew = false, pos = 1, ts = 2000L,
            ),
        )

        assertEquals(2, GachaUiMapper.map(records).gachaOverview.poolCountFes)
    }
}
