package com.blueskybone.arkscreen.ui.main

import androidx.annotation.StringRes
import com.blueskybone.arkscreen.R

enum class HomeGame(@StringRes val titleRes: Int, val bilibiliUid: String) {
    ARKNIGHTS(R.string.game_arknights, "161775300"),
    ENDFIELD(R.string.game_endfield, "1265652806");

    companion object {
        fun fromStored(value: String?): HomeGame =
            entries.firstOrNull { it.name == value } ?: ARKNIGHTS
    }
}
