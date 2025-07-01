package com.blueskybone.arkscreen.ui.bindinginfo

import com.blueskybone.arkscreen.R

/**
 *   Created by blueskybone
 *   Date: 2024/12/31
 */
sealed interface TextInfo {
    val title: Int
    val subTitle: Int
}

data object PowerSavingMode : TextInfo {
    override val title = R.string.power_saving_mode
    override val subTitle = R.string.power_saving_mode_detail
}

data object TurnOffBatteryOptimization : TextInfo {
    override val title = R.string.turn_off_battery_optimization
    override val subTitle = R.string.turn_off_battery_optimization_detail
}

data object CheckUpdate : TextInfo {
    override val title = R.string.check_update
    override val subTitle = R.string.version
}

data object GroupChat : TextInfo {
    override val title = R.string.qq_group
    override val subTitle = R.string.qq_group_content
}

data object BackAutoAtd : TextInfo {
    override val title = R.string.back_auto_attendance
    override val subTitle = R.string.back_auto_attendance_detail
}

data object OverlayPermission : TextInfo {
    override val title = R.string.overlay_permission
    override val subTitle = R.string.overlay_permission_detail
}

data object NotifyPermission : TextInfo {
    override val title = R.string.notify_permission
    override val subTitle = R.string.notify_permission_detail
}

data object OpenAutoStartSettings : TextInfo {
    override val title = R.string.open_auto_start_settings
    override val subTitle = R.string.open_auto_start_settings_detail
}

//单独绑定
data object SetAtdTime : TextInfo {
    override val title = R.string.set_auto_attendance_time
    override val subTitle = R.string.auto_attendance_time
}

data object UseInnerWeb : TextInfo {
    override val title = R.string.use_inner_web
    override val subTitle = R.string.use_inner_web_detail
}

data object WidgetRefresh : TextInfo {
    override val title = R.string.widget_refresh
    override val subTitle = R.string.widget_refresh_detail
}

data object TimeCorrection : TextInfo {
    override val title = R.string.time_correction
    override val subTitle = R.string.time_correction_detail
}