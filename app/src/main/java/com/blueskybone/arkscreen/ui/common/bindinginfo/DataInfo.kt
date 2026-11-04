package com.blueskybone.arkscreen.ui.common.bindinginfo

import com.blueskybone.arkscreen.R

/**
 *   Created by blueskybone
 *   Date: 2025/1/17
 */
sealed interface DataInfo {
    val title: Int
}

data object Recruit : DataInfo {
    override val title = R.string.recruit
}

data object RecruitRefresh : DataInfo {
    override val title = R.string.recruit_refresh
}

data object Labor : DataInfo {
    override val title = R.string.labor
}

data object Meeting : DataInfo {
    override val title = R.string.meeting
}

data object Manufactures : DataInfo {
    override val title = R.string.manufactures
}

data object Trading : DataInfo {
    override val title = R.string.trading
}

data object Dormitories : DataInfo {
    override val title = R.string.dormitories
}

data object Tired : DataInfo {
    override val title = R.string.tired
}

data object Train : DataInfo {
    override val title = R.string.train
}


