package com.blueskybone.arkscreen.ui.gacha

import androidx.annotation.ColorRes
import com.blueskybone.arkscreen.R

@ColorRes
internal fun gachaPullColor(count: Int): Int = when {
    count <= 10 -> R.color.gacha_pull_green
    count < 34 -> R.color.gacha_pull_blue
    count <= 50 -> R.color.gacha_pull_yellow
    else -> R.color.gacha_pull_red
}
