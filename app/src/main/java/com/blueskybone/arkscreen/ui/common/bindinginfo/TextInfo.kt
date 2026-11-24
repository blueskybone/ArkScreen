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

data object SetAtdTime : TextInfo {
    override val title = R.string.attendance_time
    override val subTitle = R.string.attendance_time_detail
    override val icon = R.drawable.ic_time
}

data object TimeCorrection : TextInfo {
    override val title = R.string.time_correction
    override val subTitle = R.string.time_correction_detail
    override val icon = R.drawable.ic_delay
}
