package com.blueskybone.arkscreen.ui.gacha

import com.blueskybone.arkscreen.data.local.room.EndfieldGachaPool
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EndfieldPityCalculatorTest {
    private val special = "E_CharacterGachaPoolType_Special"
    private val rerun = "E_CharacterGachaPoolType_Rerun"

    @Test fun giftsAndFreeDrawsDoNotAdvancePity() {
        val records = listOf(
            record("1", special, "a", rarity = 4),
            record("2", special, "a", kind = "gift_book", name = "寻访情报书"),
            record("3", special, "a", rarity = 6, isFree = true),
            record("4", special, "a", rarity = 4),
        )
        val stat = EndfieldPityCalculator.calculate(records, emptyList(), "char").single()
        assertEquals(3, stat.total)
        assertEquals(1, stat.free)
        assertEquals(2, stat.smallCount)
        assertEquals(1, stat.hits.size)
        assertTrue(stat.hits.single().isFree)
    }

    @Test fun specialPityCarriesBetweenPoolsAndPaidSixResetsIt() {
        val records = listOf(
            record("1", special, "a", rarity = 4),
            record("2", special, "b", rarity = 4),
            record("3", special, "b", rarity = 6, itemId = "up"),
            record("4", special, "b", rarity = 4),
        )
        val pools = listOf(EndfieldGachaPool("role", "char", "b", "池 B", "up"))
        val stats = EndfieldPityCalculator.calculate(records, pools, "char")
        assertEquals(1, stats.first().smallCount)
        assertTrue(stats.first().bigSatisfied)
        assertEquals(3, stats.first().hits.single().pity)
        assertEquals(1, stats.last().smallCount)
    }

    @Test fun rerunUsesOfficialCountAndSharesSixPity() {
        val records = listOf(
            record("1", rerun, "a", rarity = 4, name = "重构 A"),
            record("2", rerun, "b", rarity = 4, name = "重构 B"),
        )
        val pools = listOf(EndfieldGachaPool("role", "char", "b", "重构 B", "up", 70))
        val stats = EndfieldPityCalculator.calculate(records, pools, "char")
        assertEquals(2, stats.first().smallCount)
        assertEquals(70, stats.first().bigCount)
        assertTrue(stats.first().bigUsesOfficialCount)
    }

    @Test fun weaponRerunVersionResetsSmallPity() {
        val records = listOf(
            record("1", "rerun", "rerun_weapon", type = "weapon", version = 1),
            record("2", "rerun", "rerun_weapon", type = "weapon", version = 2),
        )
        val stat = EndfieldPityCalculator.calculate(records, emptyList(), "weapon").single()
        assertEquals(1, stat.smallCount)
        assertEquals(40, stat.smallMax)
        assertFalse(stat.bigSatisfied)
    }

    private fun record(
        seq: String,
        poolType: String,
        poolId: String,
        type: String = "char",
        rarity: Int = 4,
        kind: String = "draw",
        name: String = "干员",
        itemId: String = "item",
        isFree: Boolean = false,
        version: Int? = null,
    ) = EndfieldGachaRecord(
        roleId = "role", type = type, seqId = seq, poolId = poolId,
        poolName = name, poolType = poolType, itemId = itemId,
        itemName = name, rarity = rarity, gachaTs = seq, kind = kind,
        isNew = false, isFree = isFree, poolVersion = version,
    )
}
