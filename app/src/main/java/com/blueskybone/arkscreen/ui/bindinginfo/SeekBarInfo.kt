package com.blueskybone.arkscreen.ui.bindinginfo

import com.blueskybone.arkscreen.R

/**
 *   Created by blueskybone
 *   Date: 2024/12/31
 */
sealed interface SeekBarInfo {
    val title: Int
    val key: String
    val defaultValue: Int
    val min: Int
    val max: Int
    val step: Int
}

data object ScreenshotDelay : SeekBarInfo {
    override val title = R.string.screenshot_delay
    override val key = "screenshot_delay"
    override val defaultValue = 0
    override val min = 0
    override val max = 3
    override val step = 1
}

data object WidgetAlpha : SeekBarInfo {
    override val title = R.string.widget_alpha
    override val key = "widget_alpha"
    override val defaultValue = 120
    override val min = 0
    override val max = 255
    override val step = 15
}