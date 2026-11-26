package com.blueskybone.arkscreen.data.repository.mapper

import com.blueskybone.arkscreen.data.local.room.WidgetConfig as WidgetConfigEntity
import com.blueskybone.arkscreen.domain.model.widget.WidgetConfig

internal fun WidgetConfigEntity.toDomain() = WidgetConfig(
    appWidgetId = appWidgetId,
    accountMode = accountMode,
    game = game,
    serverKey = serverKey,
    roleUid = roleUid,
    theme = theme,
    layout1x2 = layout1x2,
    layout2x3 = layout2x3,
    slot1x1 = slot1x1,
    slot1x2 = slot1x2,
    slots2x2 = slots2x2,
    slots2x3 = slots2x3,
    fontScale = fontScale,
    transparency = transparency,
)

internal fun WidgetConfig.toEntity() = WidgetConfigEntity(
    appWidgetId = appWidgetId,
    accountMode = accountMode,
    game = game,
    serverKey = serverKey,
    roleUid = roleUid,
    theme = theme,
    layout1x2 = layout1x2,
    layout2x3 = layout2x3,
    slot1x1 = slot1x1,
    slot1x2 = slot1x2,
    slots2x2 = slots2x2,
    slots2x3 = slots2x3,
    fontScale = fontScale,
    transparency = transparency,
)
