package com.blueskybone.arkscreen.ui.common.bindinginfo

import androidx.annotation.ColorRes
import com.blueskybone.arkscreen.R

/**
 *   Created by blueskybone
 *   Date: 2025/6/9
 */
data class FuncChipColors(
    @param:ColorRes val icon: Int,
    @param:ColorRes val container: Int,
)

sealed interface FuncChipInfo {
    val title: Int
    val icon: Int
    val colors: FuncChipColors
}

data object RecruitCal : FuncChipInfo {
    override val title = R.string.recruit_calc
    override val icon = R.drawable.ic_tags
    override val colors = FuncChipColors(
        icon = R.color.tool_recruit_icon,
        container = R.color.tool_recruit_container,
    )
}

data object OpeAssets : FuncChipInfo {
    override val title = R.string.operator_assets
    override val icon = R.drawable.ic_chess
    override val colors = FuncChipColors(
        icon = R.color.tool_assets_icon,
        container = R.color.tool_assets_container,
    )
}

data object GachaStat : FuncChipInfo {
    override val title = R.string.gacha_statistics
    override val icon = R.drawable.ic_gacha
    override val colors = FuncChipColors(
        icon = R.color.tool_gacha_icon,
        container = R.color.tool_gacha_container,
    )
}

data object Attendance : FuncChipInfo {
    override val title = R.string.attendance_click
    override val icon = R.drawable.ic_check
    override val colors = FuncChipColors(
        icon = R.color.tool_attendance_icon,
        container = R.color.tool_attendance_container,
    )
}

data object AccountManager : FuncChipInfo {
    override val title = R.string.account_manager
    override val icon = R.drawable.ic_user
    override val colors = FuncChipColors(
        icon = R.color.tool_account_icon,
        container = R.color.tool_account_container,
    )
}

data object GameStarter : FuncChipInfo {
    override val title = R.string.game_starter
    override val icon = R.drawable.ic_notf_point
    override val colors = FuncChipColors(
        icon = R.color.tool_game_icon,
        container = R.color.tool_game_container,
    )
}

data object UserManual : FuncChipInfo {
    override val title = R.string.manual
    override val icon = R.drawable.ic_question
    override val colors = FuncChipColors(
        icon = R.color.tool_manual_icon,
        container = R.color.tool_manual_container,
    )
}
