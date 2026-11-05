package com.blueskybone.arkscreen.data.repository.mapper

import com.blueskybone.arkscreen.data.network.model.EndfieldBattlePass
import com.blueskybone.arkscreen.data.network.model.EndfieldCardDetail
import com.blueskybone.arkscreen.data.network.model.EndfieldDailyMission
import com.blueskybone.arkscreen.data.network.model.EndfieldDomain
import com.blueskybone.arkscreen.data.network.model.EndfieldMoneyManager
import com.blueskybone.arkscreen.data.network.model.EndfieldSettlement
import com.blueskybone.arkscreen.data.network.model.EndfieldStamina
import com.blueskybone.arkscreen.data.network.model.EndfieldPlayerBase
import com.blueskybone.arkscreen.data.network.model.EndfieldWeeklyMission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EndfieldRealtimeMapperTest {
    @Test
    fun usesBaseValuesAndKeepsEveryReturnedDomain() {
        val snapshot = EndfieldRealtimeMapper.toDomain(
            EndfieldCardDetail(
                base = EndfieldPlayerBase(
                    roleId = "123", name = "测试玩家", serverName = "测试服",
                    level = 42, worldLevel = 3, avatarUrl = "https://example.test/avatar.png",
                    lastLoginTime = "1700000000",
                ),
                currentTs = "1000",
                dungeon = EndfieldStamina(curStamina = "72", maxStamina = "240", maxTs = "2000"),
                dailyMission = EndfieldDailyMission(dailyActivation = 40, maxDailyActivation = 100),
                weeklyMission = EndfieldWeeklyMission(score = 300, total = 1000),
                bpSystem = EndfieldBattlePass(curLevel = 12, maxLevel = 60),
                domain = listOf(
                    EndfieldDomain(
                        domainId = "a",
                        name = "地区一",
                        level = 12,
                        moneyMgr = EndfieldMoneyManager(total = "30000000", count = "28953521"),
                        settlements = listOf(
                            EndfieldSettlement(
                                id = "s1", name = "调度站一", remainMoney = "9",
                                moneyMax = "40", lastTickTime = "900",
                            ),
                            EndfieldSettlement(
                                id = "s2", name = "调度站二", remainMoney = "12",
                                moneyMax = "40",
                            ),
                        ),
                    ),
                    EndfieldDomain(domainId = "b", name = "地区二", settlements = emptyList()),
                ),
            )
        )

        assertEquals(72L, snapshot.stamina?.baseValue)
        assertEquals("测试玩家", snapshot.profile?.name)
        assertEquals(42, snapshot.profile?.level)
        assertEquals(3, snapshot.profile?.worldLevel)
        assertEquals(1700000000L, snapshot.profile?.lastLoginAt)
        assertEquals(72L, snapshot.stamina?.displayValue)
        assertEquals(2000L, snapshot.stamina?.fullAt)
        assertEquals(40, snapshot.dailyMission?.value)
        assertEquals(300, snapshot.weeklyMission?.value)
        assertEquals(12, snapshot.battlePass?.value)
        assertEquals(2, snapshot.domains.size)
        assertEquals(2, snapshot.domains.first().settlements.size)
        assertEquals(12, snapshot.domains.first().level)
        assertEquals(28953521L, snapshot.domains.first().dispatchTickets?.displayValue)
        assertEquals(30000000L, snapshot.domains.first().dispatchTickets?.maxValue)
        val tickets = snapshot.domains.first().settlements.first().dispatchTickets
        assertEquals(9L, tickets.baseValue)
        assertEquals(9L, tickets.displayValue)
        assertEquals(900L, tickets.lastTickAt)
        assertEquals(0, snapshot.domains.last().settlements.size)
    }

    @Test
    fun missingAndInvalidValuesRemainUnknown() {
        val snapshot = EndfieldRealtimeMapper.toDomain(
            EndfieldCardDetail(
                dungeon = EndfieldStamina(curStamina = "unknown", maxStamina = null),
                domain = null,
            )
        )

        assertNull(snapshot.stamina?.displayValue)
        assertNull(snapshot.stamina?.maxValue)
        assertEquals(emptyList<Any>(), snapshot.domains)
    }
}
