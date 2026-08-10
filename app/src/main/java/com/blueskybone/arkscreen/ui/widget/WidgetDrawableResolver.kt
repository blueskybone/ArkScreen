package com.blueskybone.arkscreen.ui.widget

import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetTextColor

fun getTargetDrawableId(drawable: Int, textColor: String): Int {
    if (textColor != WidgetTextColor.BLACK) return drawable
    return when (drawable) {
        R.drawable.ic_drone -> R.drawable.ic_drone_black
        R.drawable.ic_bolt -> R.drawable.ic_bolt_black
        R.drawable.ic_train -> R.drawable.ic_train_black
        R.drawable.ic_clue -> R.drawable.ic_clue_black
        else -> R.drawable.ic_default
    }
}
