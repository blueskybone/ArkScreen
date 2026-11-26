package com.blueskybone.arkscreen.domain.model.settings

/** Stable persisted values and defaults. Display labels belong to the UI layer. */
object FloatWindowAppearanceSetting {
    const val key = "float_window_appearance"
    const val SIMPLE = "simple"
    const val COLORFUL = "colorful"
    const val defaultValue = COLORFUL
    val values = listOf(SIMPLE, COLORFUL)
}

object ScreenshotDelaySetting {
    const val key = "screenshot_delay"
    const val ONE_SECOND = "1000"
    const val TWO_SECOND = "2000"
    const val THREE_SECOND = "3000"
    const val defaultValue = ONE_SECOND
    val values = listOf(ONE_SECOND, TWO_SECOND, THREE_SECOND)
    fun getMSec(value: String): Long = when (value) {
        ONE_SECOND -> 1000L
        TWO_SECOND -> 2000L
        THREE_SECOND -> 3000L
        else -> throw IllegalArgumentException("Invalid : $value")
    }
}

object AppThemeSetting {
    const val key = "app_theme"
    const val LIGHT = "light"
    const val DARK = "dark"
    const val SYSTEM = "system"
    const val defaultValue = SYSTEM
    val values = listOf(LIGHT, DARK, SYSTEM)
}

object RecruitModeSetting {
    const val key = "recruit_mode"
    const val FLOATWINDOW = "floatWindow"
    const val TOAST = "toast"
    const val AUTO = "Auto"
    const val defaultValue = FLOATWINDOW
    val values = listOf(FLOATWINDOW, TOAST, AUTO)
}

object WidgetUpdateFrequencySetting {
    const val key = "widget_update_freq"
    const val MIN_15 = "min_15"
    const val MIN_30 = "min_30"
    const val HOUR_1 = "hour_1"
    const val defaultValue = MIN_15
    val values = listOf(MIN_15, MIN_30, HOUR_1)
    fun getValue(value: String): Int = when (value) {
        MIN_15 -> 900
        MIN_30 -> 1800
        HOUR_1 -> 3600
        else -> 1800
    }
}
