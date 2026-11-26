package com.blueskybone.arkscreen.ui.common

import androidx.annotation.StringRes

/** Presentation-only resource access; ViewModels never retain an Activity or View. */
interface UiStrings {
    fun get(@StringRes id: Int, vararg args: Any): String
}
