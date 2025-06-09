package com.blueskybone.arkscreen.bindinginfo

import android.graphics.Color
import com.blueskybone.arkscreen.R

/**
 *   Created by blueskybone
 *   Date: 2025/6/9
 */
sealed interface FuncChipInfo {
    val title: Int
    val icon: Int
    val bgColor: Int
    val iconColor: Int
}

data object RecruitCal : FuncChipInfo {
    override val title = R.string.recruit_cal
    override val icon = R.drawable.ic_tags
    override val bgColor = R.color.LightBlue200
    override val iconColor = R.color.blue_500
}

data object OpeAssets : FuncChipInfo {
    override val title = R.string.operator_assets
    override val icon = R.drawable.ic_tags
    override val bgColor = R.color.LightBlue200
    override val iconColor = R.color.blue_500
}

data object GachaStat : FuncChipInfo {
    override val title = R.string.gacha_statistics
    override val icon = R.drawable.ic_tags
    override val bgColor = R.color.LightBlue200
    override val iconColor = R.color.blue_500
}

data object Attendance : FuncChipInfo {
    override val title = R.string.attendance_click
    override val icon = R.drawable.ic_tags
    override val bgColor = R.color.LightBlue200
    override val iconColor = R.color.blue_500
}