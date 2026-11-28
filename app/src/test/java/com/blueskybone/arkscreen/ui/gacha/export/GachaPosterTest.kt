package com.blueskybone.arkscreen.ui.gacha.export

import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.ui.gacha.GachaUiState
import com.blueskybone.arkscreen.ui.gacha.model.*
import org.junit.Assert.*
import org.junit.Test

class GachaPosterTest {
    @Test fun selectedPoolKeepsAllHitsAndUnfinishedPullsDespiteRecordFilters() {
        val state = state().copy(selectedPoolId = "a", filterNew = true, filterSixStar = true)
        val poster = requireNotNull(GachaPoster.from(state))
        assertEquals(80, poster.total)
        assertEquals(2, poster.sixStars)
        assertEquals(40.0, poster.average!!, 0.001)
        assertEquals(listOf("hit1", "hit2"), poster.pools.single().hitRecords.map { it.id })
        assertEquals(10L, poster.firstTimestamp)
        assertEquals(30L, poster.lastTimestamp)
    }

    @Test fun emptyPoolsAreHiddenWithoutChangingSummaryTotals() {
        val poster = requireNotNull(GachaPoster.from(state()))
        assertEquals(listOf("a"), poster.visiblePools.map { it.poolId })
        assertEquals(2, poster.pools.size)
        assertEquals(120, poster.total)
        assertEquals(60.0, poster.average!!, 0.001)
    }

    @Test fun noSixStarsHasNoAverageAndMissingAccountCannotExport() {
        val emptyPoolPoster = GachaPoster.from(state().copy(selectedPoolId = "b"))!!
        assertNull(emptyPoolPoster.average)
        assertTrue(emptyPoolPoster.visiblePools.isEmpty())
        assertNull(GachaPoster.from(state().copy(currAccount = null)))
        assertNull(GachaPoster.from(state().copy(selectedPoolId = "missing")))
    }

    private fun state(): GachaUiState {
        val first = record("hit1", "a", 10, 20)
        val second = record("hit2", "a", 20, 30)
        val snapshot = GachaUiSnapshot(
            records = listOf(first, second, record("unfinished", "a", 30, 30).copy(rare = 3),
                record("other", "b", 40, 40).copy(rare = 3)),
            gachaPoolStats = emptyList(), gachaSummary = GachaSummary(),
            gachaPools = listOf(GachaPool("A", "a", false, listOf(first, second), 80, 30),
                GachaPool("B", "b", true, emptyList(), 40, 40)),
            gachaOverview = GachaOverview(),
        )
        return GachaUiState(currAccount = AccountGc("uid", "name", "", true, 1, "", ""),
            gachaUiSnapshot = snapshot)
    }

    private fun record(id: String, pool: String, ts: Long, count: Int) =
        Record(id, "operator", "char", false, count, ts, 0, 5, pool, pool, count)
}
