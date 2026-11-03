package com.blueskybone.arkscreen.data.repository.mapper

import com.blueskybone.arkscreen.data.network.model.EndfieldCardDetail
import com.blueskybone.arkscreen.domain.model.realtime.EndfieldDomainStatus
import com.blueskybone.arkscreen.domain.model.realtime.EndfieldProgress
import com.blueskybone.arkscreen.domain.model.realtime.EndfieldPlayerProfile
import com.blueskybone.arkscreen.domain.model.realtime.EndfieldRealtimeSnapshot
import com.blueskybone.arkscreen.domain.model.realtime.EndfieldRecoverableValue
import com.blueskybone.arkscreen.domain.model.realtime.EndfieldSettlementStatus

object EndfieldRealtimeMapper {
    fun toDomain(detail: EndfieldCardDetail): EndfieldRealtimeSnapshot =
        EndfieldRealtimeSnapshot(
            profile = detail.base?.let { base ->
                EndfieldPlayerProfile(
                    roleId = base.roleId,
                    name = base.name,
                    serverName = base.serverName,
                    level = base.level,
                    worldLevel = base.worldLevel,
                    avatarUrl = base.avatarUrl,
                    lastLoginAt = base.lastLoginTime?.toLongOrNull(),
                )
            },
            stamina = detail.dungeon?.let { dungeon ->
                EndfieldRecoverableValue(
                    baseValue = dungeon.curStamina?.toLongOrNull(),
                    maxValue = dungeon.maxStamina?.toLongOrNull(),
                    sampledAt = detail.currentTs?.toLongOrNull(),
                    fullAt = dungeon.maxTs?.toLongOrNull(),
                )
            },
            dailyMission = detail.dailyMission?.let {
                EndfieldProgress(it.dailyActivation, it.maxDailyActivation)
            },
            weeklyMission = detail.weeklyMission?.let {
                EndfieldProgress(it.score, it.total)
            },
            battlePass = detail.bpSystem?.let {
                EndfieldProgress(it.curLevel, it.maxLevel)
            },
            domains = detail.domain.orEmpty().map { domain ->
                EndfieldDomainStatus(
                    id = domain.domainId,
                    name = domain.name,
                    level = domain.level,
                    dispatchTickets = domain.moneyMgr?.let { money ->
                        EndfieldRecoverableValue(
                            baseValue = money.count?.toLongOrNull(),
                            maxValue = money.total?.toLongOrNull(),
                            sampledAt = detail.currentTs?.toLongOrNull(),
                        )
                    },
                    settlements = domain.settlements.orEmpty().map { settlement ->
                        EndfieldSettlementStatus(
                            id = settlement.id,
                            name = settlement.name,
                            dispatchTickets = EndfieldRecoverableValue(
                                baseValue = settlement.remainMoney?.toLongOrNull(),
                                maxValue = settlement.moneyMax?.toLongOrNull(),
                                sampledAt = detail.currentTs?.toLongOrNull(),
                                lastTickAt = settlement.lastTickTime?.toLongOrNull(),
                            ),
                        )
                    },
                )
            },
        )
}
