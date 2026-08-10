package com.blueskybone.arkscreen.ui.common.bindinginfo

import android.content.Context
import androidx.core.content.ContextCompat.getString
import com.blueskybone.arkscreen.R

/**
 *   Created by blueskybone
 *   Date: 2024/12/31
 */

sealed interface ListInfo {
    val title: Int
    val key: String
    val defaultValue: String
    fun getEntryValues(): Array<String>
    fun getEntries(context: Context): Array<String>
}


data object FloatWindowAppearance : ListInfo {
    const val SIMPLE = "simple"
    const val COLORFUL = "colorful"

    override val key = "float_window_appearance"
    override val title = R.string.float_window_appearance
    override val defaultValue = COLORFUL

    override fun getEntryValues() = arrayOf(
        SIMPLE, COLORFUL
    )

    override fun getEntries(context: Context): Array<String> {
        val simple = getString(context, R.string.simple)
        val colorful = getString(context, R.string.colorful)
        return arrayOf(simple, colorful)
    }
}

data object ScDelay: ListInfo {
    const val ONE_SECOND = "1000"
    const val TWO_SECOND = "2000"
    const val THREE_SECOND = "3000"

    override val key = "screenshot_delay"
    override val title = R.string.screenshot_delay
    override val defaultValue = ONE_SECOND

    override fun getEntryValues() = arrayOf(
        ONE_SECOND, TWO_SECOND, THREE_SECOND
    )

    override fun getEntries(context: Context): Array<String> {
        val sec_1 = getString(context, R.string.sec_1)
        val sec_2 = getString(context, R.string.sec_2)
        val sec_3 = getString(context, R.string.sec_3)
        return arrayOf(sec_1, sec_2, sec_3)
    }

    fun getMSec(sec:String):Long{
        return when (sec) {
            ONE_SECOND -> 1000L
            TWO_SECOND -> 2000L
            THREE_SECOND -> 3000L
            else -> throw IllegalArgumentException("Invalid : $sec")
        }
    }

}

data object AppTheme : ListInfo {
    const val LIGHT = "light"
    const val DARK = "dark"
    const val SYSTEM = "system"

    override val key = "app_theme"
    override val title = R.string.app_theme
    override val defaultValue = SYSTEM

    override fun getEntryValues() = arrayOf(
        LIGHT, DARK, SYSTEM
    )

    override fun getEntries(context: Context): Array<String> {
        val light = getString(context, R.string.theme_light)
        val dark = getString(context, R.string.theme_dark)
        val system = getString(context, R.string.theme_system)
        return arrayOf(light, dark, system)
    }
}


data object RecruitMode : ListInfo {
    const val FLOATWINDOW = "floatWindow"
    const val TOAST = "toast"
    const val AUTO = "Auto"

    override val key = "recruit_mode"
    override val title = R.string.recruit_show_mode
    override val defaultValue = FLOATWINDOW

    override fun getEntryValues() = arrayOf(
        FLOATWINDOW, TOAST, AUTO
    )

    override fun getEntries(context: Context): Array<String> {
        val floatWindow = getString(context, R.string.float_win)
        val toast = getString(context, R.string.toast)
        val auto = getString(context, R.string.auto)
        return arrayOf(floatWindow, toast, auto)
    }
}


data object WidgetUpdateFreq : ListInfo {
    private const val MIN_15 = "min_15"
    private const val MIN_30 = "min_30"
    private const val HOUR_1 = "hour_1"
    override val title: Int = R.string.widget_update_freq
    override val key: String = "widget_update_freq"
    override val defaultValue: String = MIN_15
    override fun getEntryValues() = arrayOf(MIN_15, MIN_30, HOUR_1)

    override fun getEntries(context: Context): Array<String> {
        val min15 = getString(context, R.string.min_15)
        val min30 = getString(context, R.string.min_30)
        val hour1 = getString(context, R.string.hour_1)
        return arrayOf(min15, min30, hour1)
    }

    fun getValue(str: String): Int {
        return when (str) {
            MIN_15 -> 900
            MIN_30 -> 1800
            HOUR_1 -> 3600
            else -> 1800
        }
    }
}
