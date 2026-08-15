package com.blueskybone.arkscreen.data.network

import com.blueskybone.arkscreen.domain.model.realtime.RealTimeData
import java.net.URLEncoder

fun RealTimeData.Avatar.resolveUrl(): String =
    when (type) {
        "ASSISTANT" -> "$avatarUrl${URLEncoder.encode(id, Charsets.UTF_8.name())}.png"
        else -> url
    }
