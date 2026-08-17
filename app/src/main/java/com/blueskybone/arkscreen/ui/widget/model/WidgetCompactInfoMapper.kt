package com.blueskybone.arkscreen.ui.widget.model

import com.blueskybone.arkscreen.data.local.pref.CachePrefManager
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.ui.widget.WidgetContentFormatter

/**
 * 将共享的组件状态投影为 1x1 使用的两行紧凑信息。文本直接由源缓存计算，
 * 不从 [WidgetInfoItem.value] 反向解析，避免展示格式变化影响业务数据。
 */
class WidgetCompactInfoMapper(
    cache: CachePrefManager,
    appClock: AppClock,
) {
    private val formatter = WidgetContentFormatter(cache, appClock)

    fun map(item: WidgetInfoItem): WidgetCompactItem {
        val content = formatter.format(item.type.contentKey, compact = true)
        return WidgetCompactItem(
            type = item.type,
            primary = content.primary,
            supporting = content.secondary.takeIf(String::isNotBlank),
            state = item.state,
            icon = item.icon,
        )
    }

    private val WidgetInfoType.contentKey: String
        get() = when (this) {
            WidgetInfoType.SANITY -> "ap"
            WidgetInfoType.DRONE -> "labor"
            WidgetInfoType.RECRUITMENT -> "recruit"
            WidgetInfoType.RECRUITMENT_REFRESH -> "refresh"
            WidgetInfoType.TRAINING -> "train"
            WidgetInfoType.MEETING -> "meet"
            WidgetInfoType.ATTENDANCE -> error("Attendance is not selectable in 1x1 widgets")
        }
}
