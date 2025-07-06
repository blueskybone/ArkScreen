package com.blueskybone.arkscreen.ui.bindinginfo

import android.content.Context
import android.graphics.Color
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


data object WidgetAppearance : ListInfo {
    const val blackOnWhite = "blackOnWhite"
    const val whiteOnBlack = "whiteOnBlack" //替代enum

    override val key = "widget_appearance"
    override val title = R.string.widget_appearance
    override val defaultValue = whiteOnBlack

    override fun getEntryValues() = arrayOf(blackOnWhite, whiteOnBlack)

    override fun getEntries(context: Context): Array<String> {
        val wob = getString(context, R.string.white_on_black)
        val bow = getString(context, R.string.black_on_white)
        return arrayOf(wob, bow)
    }
}

data object FloatWindowAppearance : ListInfo {
    const val simple = "simple"
    const val colorful = "colorful"

    override val key = "float_window_appearance"
    override val title = R.string.float_window_appearance
    override val defaultValue = colorful

    override fun getEntryValues() = arrayOf(
        simple, colorful
    )

    override fun getEntries(context: Context): Array<String> {
        val simple = getString(context, R.string.simple)
        val colorful = getString(context, R.string.colorful)
        return arrayOf(simple, colorful)
    }
}

data object AppTheme : ListInfo {
    const val light = "light"
    const val dark = "dark"
    const val system = "system"

    override val key = "app_theme"
    override val title = R.string.app_theme
    override val defaultValue = system

    override fun getEntryValues() = arrayOf(
        light, dark, system
    )

    override fun getEntries(context: Context): Array<String> {
        val light = getString(context, R.string.theme_light)
        val dark = getString(context, R.string.theme_dark)
        val system = getString(context, R.string.theme_system)
        return arrayOf(light, dark, system)
    }
}


data object RecruitMode : ListInfo {
    const val floatWindow = "floatWindow"
    const val toast = "toast"
    const val auto = "Auto"

    override val key = "recruit_mode"
    override val title = R.string.recruit_show_mode
    override val defaultValue = floatWindow

    override fun getEntryValues() = arrayOf(
        floatWindow, toast, auto
    )

    override fun getEntries(context: Context): Array<String> {
        val floatWindow = getString(context, R.string.float_win)
        val toast = getString(context, R.string.toast)
        val auto = getString(context, R.string.auto)
        return arrayOf(floatWindow, toast, auto)
    }
}


data object WidgetSize : ListInfo {
    private const val SMALL = "small"
    private const val MEDIUM = "medium"
    private const val LARGE = "large"
    override val title: Int = R.string.widget_size
    override val key: String = "widget_size"
    override val defaultValue: String = MEDIUM

    override fun getEntryValues() = arrayOf(SMALL, MEDIUM, LARGE)

    override fun getEntries(context: Context): Array<String> {
        val small = getString(context, R.string.small)
        val medium = getString(context, R.string.medium)
        val large = getString(context, R.string.large)
        return arrayOf(small, medium, large)
    }

    //remember spToPx
    fun getTextSizeMain(size: String): Float {
        return when (size) {
            SMALL -> 20f
            MEDIUM -> 24f
            LARGE -> 28f
            else -> throw IllegalArgumentException("Invalid : $size")
        }
    }

    fun getTextSizeSub(size: String): Float {
        return when (size) {
            SMALL -> 10f
            MEDIUM -> 12f
            LARGE -> 14f
            else -> throw IllegalArgumentException("Invalid : $size")
        }
    }
    //remember dpToPx
    fun getImageSize(size: String): Int {
        return when (size) {
            SMALL -> 20
            MEDIUM -> 24
            LARGE -> 28
            else -> throw IllegalArgumentException("Invalid : $size")
        }
    }

    fun getIconSize(size: String): Int {
        return when (size) {
            SMALL -> 10
            MEDIUM -> 12
            LARGE -> 14
            else -> throw IllegalArgumentException("Invalid : $size")
        }
    }
}

data object WidgetTextColor : ListInfo {
    private const val WHITE = "white"
    private const val BLACK = "black"
    override val title: Int = R.string.text_color
    override val key: String = "widget_text_color"
    override val defaultValue: String = WHITE
    override fun getEntryValues() = arrayOf(WHITE, BLACK)
    override fun getEntries(context: Context): Array<String> {
        val white = getString(context, R.string.white)
        val black = getString(context, R.string.black)
        return arrayOf(white, black)
    }

    fun getColorInt(color: String): Int {
        return when (color) {
            WHITE -> Color.WHITE
            BLACK -> Color.BLACK
            else -> throw IllegalArgumentException("Invalid : $color")
        }
    }
}

data object WidgetContent : ListInfo {
    private const val AP = "ap"
    private const val LABOR = "labor"
    private const val TRAIN = "train"
    override val title: Int = R.string.widget_content
    override val key: String = "widget_content"
    override val defaultValue: String = AP
    const val defaultValue2: String = LABOR
    override fun getEntryValues() = arrayOf(AP, LABOR, TRAIN)

    override fun getEntries(context: Context): Array<String> {
        val ap = getString(context, R.string.ap)
        val labor = getString(context, R.string.labor)
        val train = getString(context, R.string.train)
        return arrayOf(ap, labor, train)
    }

    fun getDrawableIcon(icon: String): Int {
        return when (icon) {
            AP -> R.drawable.ic_bolt
            LABOR -> R.drawable.ic_drone
            TRAIN -> R.drawable.ic_train
            else -> throw IllegalArgumentException("Invalid : $icon")
        }
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



