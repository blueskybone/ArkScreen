package com.blueskybone.arkscreen.ui.widget.model

import androidx.annotation.DrawableRes
import com.blueskybone.arkscreen.R

/**
 * Stable identity used by content selection, persistence and template slot rules.
 *
 * A type does not describe the current API status. For example, training remains
 * [TRAINING] whether it is idle, running or completed.
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
 * Semantic presentation state after raw game data has been interpreted.
 *
 * Styles map this state to color and emphasis. Raw Skland status codes must be
 * converted before reaching this model.
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
 * Display-ready widget information shared by previews and RemoteViews renderers.
 *
 * [value] and [restTime] are nullable because some types, such as meeting,
 * may only have a meaningful remaining time.
 */
data class WidgetInfoItem(
    val type: WidgetInfoType,
    val title: String,
    val value: String?,
    val restTime: String?,
    val state: WidgetInfoState,
    val icon: WidgetInfoIcon = type.defaultIcon,
)

/** Two-line projection used by the space-constrained 1x1 template. */
data class WidgetCompactItem(
    val type: WidgetInfoType,
    val primary: String,
    val supporting: String?,
    val state: WidgetInfoState,
    val icon: WidgetInfoIcon = type.defaultIcon,
)
