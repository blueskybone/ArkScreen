package com.blueskybone.arkscreen.ui.widget.model

import androidx.annotation.DrawableRes
import com.blueskybone.arkscreen.R

/**
 * 用于内容选择、持久化和模板槽位规则的稳定标识。
 *
 * 类型不描述当前接口状态。例如训练室无论空闲、进行中还是已完成，都保持为 [TRAINING]。
 */
enum class WidgetInfoType(
    val defaultIcon: WidgetInfoIcon,
) {
    SANITY(WidgetInfoIcon(R.drawable.ic_bolt)),
    DRONE(WidgetInfoIcon(R.drawable.ic_drone)),
    RECRUITMENT(WidgetInfoIcon(R.drawable.ic_recruit)),
    RECRUITMENT_REFRESH(WidgetInfoIcon(R.drawable.ic_refresh)),
    TRAINING(WidgetInfoIcon(R.drawable.ic_train)),
    MEETING(WidgetInfoIcon(R.drawable.ic_clue)),
    ATTENDANCE(WidgetInfoIcon(R.drawable.ic_widget_attendance)),
}

/**
 * 原始游戏数据解释完成后的语义化展示状态。
 *
 * 样式层根据该状态决定颜色和强调方式；森空岛原始状态码必须先转换再进入本模型。
 */
enum class WidgetInfoState {
    NORMAL,
    ATTENTION,
    COMPLETED,
    IDLE,
    DISABLED,
    ERROR,
    UNAVAILABLE,
}

data class WidgetInfoIcon(
    @param:DrawableRes val drawableRes: Int,
)

/**
 * 预览和 RemoteViews 渲染器共用的、可直接展示的桌面组件信息。
 *
 * [value] 和 [restTime] 可空，因为会客室等类型可能只有剩余时间具备展示意义。
 */
data class WidgetInfoItem(
    val type: WidgetInfoType,
    val title: String,
    val value: String?,
    val restTime: String?,
    val state: WidgetInfoState,
    val icon: WidgetInfoIcon = type.defaultIcon,
)

/** 空间受限的 1x1 模板使用的两行投影数据。 */
data class WidgetCompactItem(
    val type: WidgetInfoType,
    val primary: String,
    val supporting: String?,
    val state: WidgetInfoState,
    val icon: WidgetInfoIcon = type.defaultIcon,
)
