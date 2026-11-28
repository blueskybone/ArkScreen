package com.blueskybone.arkscreen.ui.gacha

import com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaRecord
import org.junit.Assert.*
import org.junit.Test

class EndfieldGachaPresentationTest {
    @Test fun summaryExcludesOtherCategoryAndGiftsButCountsFreeHitsSeparately() {
        val data = buildEndfieldGachaPresentation(EndfieldGachaInput("role", "char", listOf(
            record(1), record(2, rarity = 6), record(3, rarity = 6, free = true),
            record(4, rarity = 5), record(5, type = "weapon", rarity = 6),
            record(6, rarity = 6).copy(kind = "gift"),
        ), emptyList()))
        assertEquals(listOf("4", "3", "2", "1"), data.records.map { it.seqId })
        assertEquals(2, data.six)
        assertEquals(1, data.five)
        assertEquals(1, data.free)
        assertEquals(2.0, data.average!!, 0.001)
        assertEquals(2, data.best)
        assertEquals(2, data.worst)
    }

    @Test fun rawFiltersPreserveOriginalPityAndPoolTotals() {
        val data = buildEndfieldGachaPresentation(EndfieldGachaInput("role", "char", listOf(
            record(1), record(2, pool = "other"), record(3), record(4, rarity = 6).copy(isNew = true),
        ), emptyList()))
        val rows = data.filterRows(EndfieldRecordFilter(data.scope, "pool", sixOnly = true, newOnly = true))
        assertEquals(1, rows.size)
        assertEquals(3, rows.single().pity)
        assertEquals(3, rows.single().total)
        assertEquals(4, data.rows.size)
    }

    @Test fun foreignOrMissingPoolSelectionFallsBackToAllPools() {
        val data = buildEndfieldGachaPresentation(EndfieldGachaInput("role", "char", listOf(
            record(1), record(2, pool = "other"),
        ), emptyList()))
        assertEquals(2, data.filterRows(EndfieldRecordFilter("another:char", "pool")).size)
        assertEquals(2, data.filterRows(EndfieldRecordFilter(data.scope, "missing")).size)
    }

    @Test fun emptyHistoryHasNoAverageOrExtremes() {
        val data = buildEndfieldGachaPresentation(EndfieldGachaInput(null, "weapon", emptyList(), emptyList()))
        assertTrue(data.rows.isEmpty())
        assertNull(data.average)
        assertNull(data.best)
        assertNull(data.worst)
    }

    @Test fun averageIncludesUnfinishedPullsAcrossPoolsForCharactersAndWeapons() {
        for (type in listOf("char", "weapon")) {
            val records = (1..7).map { seq ->
                record(seq, type = type, pool = if (seq <= 3) "first" else "second",
                    rarity = if (seq == 2 || seq == 4) 6 else 4, free = seq == 4)
            }
            val data = buildEndfieldGachaPresentation(EndfieldGachaInput("role", type, records, emptyList()))
            assertEquals(7, data.records.size)
            assertEquals(2, data.six)
            assertEquals(3.5, data.average!!, 0.001)
        }
    }

    @Test fun nonemptyHistoryWithoutSixStarsHasNoAverage() {
        val data = buildEndfieldGachaPresentation(EndfieldGachaInput("role", "char",
            listOf(record(1), record(2)), emptyList()))
        assertNull(data.average)
    }

    private fun record(seq: Int, type: String = "char", pool: String = "pool", rarity: Int = 4, free: Boolean = false) =
        EndfieldGachaRecord("role", type, "$seq", pool, pool, "E_CharacterGachaPoolType_Special",
            "item", "name", rarity, "$seq", "draw", false, free)
}
