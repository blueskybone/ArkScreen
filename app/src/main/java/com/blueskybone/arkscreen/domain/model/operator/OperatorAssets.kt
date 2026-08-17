package com.blueskybone.arkscreen.domain.model.operator

import com.blueskybone.arkscreen.domain.model.realtime.RealTimeData

data class OperatorAssets(
    val operators: List<Operator>,
    val avatar: RealTimeData.Avatar,
)
