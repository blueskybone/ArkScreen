package com.blueskybone.arkscreen.ui.bindinginfo

import com.blueskybone.arkscreen.R

/**
 *   Created by blueskybone
 *   Date: 2025/1/17
 */
sealed interface DataInfo {
    val title: Int
    val color: Int
}

data object Recruit : DataInfo {
    override val color = R.color.blue_500
    override val title = R.string.recruit
}

data object RecruitRefresh : DataInfo {
    override val color = R.color.blue_500
    override val title = R.string.recruit_refresh
}

data object Labor : DataInfo {
    override val color = R.color.purple
    override val title = R.string.labor
}

data object Meeting : DataInfo {
    override val color = R.color.blue_500
    override val title = R.string.meeting
}

data object Manufactures : DataInfo {
    override val color = R.color.blue_500
    override val title = R.string.manufactures
}

data object Trading : DataInfo {
    override val color = R.color.blue_500
    override val title = R.string.trading
}

data object Dormitories : DataInfo {
    override val color = R.color.blue_500
    override val title = R.string.dormitories
}

data object Tired : DataInfo {
    override val color = R.color.red
    override val title = R.string.tired
}

data object Train : DataInfo {
    override val color = R.color.blue_500
    override val title = R.string.train
}

data object Campaign : DataInfo {
    override val color = R.color.red
    override val title = R.string.campaign
}


