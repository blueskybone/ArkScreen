package com.blueskybone.arkscreen.ui.common.bindinginfo

import com.blueskybone.arkscreen.R

/**
 *   Created by blueskybone
 *   Date: 2024/12/31
 */
sealed interface TextInfo {
    val title: Int
    val subTitle: Int
    val icon: Int?
}

data object PowerSavingMode : TextInfo {
    override val title = R.string.power_saving_mode
    override val subTitle = R.string.power_saving_mode_detail
    override val icon = R.drawable.ic_question
}

data object ScreenshotDelay : TextInfo {
    override val title = R.string.screenshot_delay
    override val subTitle = R.string.screenshot_delay_detail
    override val icon = R.drawable.ic_delay
}

data object TurnOffBatteryOptimization : TextInfo {
    override val title = R.string.turn_off_battery_optimization
    override val subTitle = R.string.turn_off_battery_optimization_detail
    override val icon = null
}

data object CheckUpdate : TextInfo {
    override val title = R.string.check_update
    override val subTitle = R.string.version
    override val icon = R.drawable.ic_refresh
}

data object GroupChat : TextInfo {
    override val title = R.string.qq_group
    override val subTitle = R.string.qq_group_content
    override val icon = R.drawable.ic_group
}

data object BackAutoAtd : TextInfo {
    override val title = R.string.back_auto_attendance
    override val subTitle = R.string.back_auto_attendance_detail
    override val icon = R.drawable.ic_check
}

data object OverlayPermission : TextInfo {
    override val title = R.string.overlay_permission
    override val subTitle = R.string.overlay_permission_detail
    override val icon = null
}

data object NotifyPermission : TextInfo {
    override val title = R.string.notify_permission
    override val subTitle = R.string.notify_permission_detail
    override val icon = null
}

data object OpenAutoStartSettings : TextInfo {
    override val title = R.string.open_auto_start_settings
    override val subTitle = R.string.open_auto_start_settings_detail
    override val icon = null
}

data object SetAtdTime : TextInfo {
    override val title = R.string.attendance_time
    override val subTitle = R.string.attendance_time_detail
    override val icon = R.drawable.ic_time
}

data object UseInnerWeb : TextInfo {
    override val title = R.string.use_inner_web
    override val subTitle = R.string.use_inner_web_detail
    override val icon = R.drawable.ic_link
}

data object TimeCorrection : TextInfo {
    override val title = R.string.time_correction
    override val subTitle = R.string.time_correction_detail
    override val icon = R.drawable.ic_delay
}