package com.blueskybone.arkscreen.domain.model.realtime

/** 显示接口最近一次同步值。 */
data class EndfieldRecoverableValue(
    val baseValue: Long?,
    val maxValue: Long?,
    val sampledAt: Long? = null,
    val lastTickAt: Long? = null,
    val fullAt: Long? = null,
) {
    val displayValue: Long? get() = baseValue
}

data class EndfieldProgress(
    val value: Int?,
    val maximum: Int?,
)

data class EndfieldPlayerProfile(
    val roleId: String?,
    val name: String?,
    val serverName: String?,
    val level: Int?,
    val worldLevel: Int?,
    val avatarUrl: String?,
    val lastLoginAt: Long?,
)

data class EndfieldSettlementStatus(
    val id: String?,
    val name: String?,
    val dispatchTickets: EndfieldRecoverableValue,
)

data class EndfieldDomainStatus(
    val id: String?,
    val name: String?,
    val level: Int? = null,
    val dispatchTickets: EndfieldRecoverableValue? = null,
    val settlements: List<EndfieldSettlementStatus>,
)

data class EndfieldRealtimeSnapshot(
    val profile: EndfieldPlayerProfile?,
    val stamina: EndfieldRecoverableValue?,
    val dailyMission: EndfieldProgress?,
    val weeklyMission: EndfieldProgress?,
    val battlePass: EndfieldProgress?,
    val domains: List<EndfieldDomainStatus>,
)
