package com.blueskybone.arkscreen.data.network.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** 终末地 card/detail 的首批字段。未使用的响应字段留待对应页面接入时建模。 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldCardDetailResponse(
    val code: Int? = null,
    val message: String? = null,
    val data: EndfieldCardDetailData? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldCardDetailData(
    val detail: EndfieldCardDetail? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldCardDetail(
    val currentTs: String? = null,
    val dungeon: EndfieldStamina? = null,
    val dailyMission: EndfieldDailyMission? = null,
    val weeklyMission: EndfieldWeeklyMission? = null,
    val bpSystem: EndfieldBattlePass? = null,
    val domain: List<EndfieldDomain>? = null,
    val chars: List<EndfieldCharacter>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldStamina(
    val curStamina: String? = null,
    val maxStamina: String? = null,
    val maxTs: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldDailyMission(
    val dailyActivation: Int? = null,
    val maxDailyActivation: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldWeeklyMission(
    val score: Int? = null,
    val total: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldBattlePass(
    val curLevel: Int? = null,
    val maxLevel: Int? = null,
)

/** 地区数量由服务端决定，不预设固定地区或固定设施数量。 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldDomain(
    val domainId: String? = null,
    val name: String? = null,
    val moneyMgr: EndfieldMoneyManager? = null,
    val settlements: List<EndfieldSettlement>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldMoneyManager(
    val total: String? = null,
    val count: String? = null,
)

/** 调度券相关数值与时间先保持接口原始类型，恢复规则确认后再做领域映射。 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldSettlement(
    val id: String? = null,
    val name: String? = null,
    val remainMoney: String? = null,
    val moneyMax: String? = null,
    val lastTickTime: String? = null,
)
