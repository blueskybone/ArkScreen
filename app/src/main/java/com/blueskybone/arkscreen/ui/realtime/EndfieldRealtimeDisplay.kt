package com.blueskybone.arkscreen.ui.realtime

import java.math.BigDecimal
import java.math.RoundingMode

internal object EndfieldRealtimeDisplay {
    fun tenThousands(value: Long?): String = value?.let {
        BigDecimal.valueOf(it).divide(BigDecimal.valueOf(10_000), 2, RoundingMode.HALF_UP)
            .stripTrailingZeros().toPlainString() + "w"
    } ?: "—"

    fun progress(value: Long?, maximum: Long?): Int? {
        if (value == null || maximum == null || maximum <= 0) return null
        return (value.toDouble() / maximum * 100).toInt().coerceIn(0, 100)
    }
}
