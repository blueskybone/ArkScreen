package com.blueskybone.arkscreen.ui.bindinginfo

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat.getString
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetContent.ap
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetSize.large
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetSize.medium
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetSize.small
import kotlin.math.min

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
    private const val small = "small"
    private const val medium = "medium"
    private const val large = "large"
    override val title: Int = R.string.widget_size
    override val key: String = "widget_size"
    override val defaultValue: String = medium

    override fun getEntryValues() = arrayOf(small, medium, large)

    override fun getEntries(context: Context): Array<String> {
        val small = getString(context, R.string.small)
        val medium = getString(context, R.string.medium)
        val large = getString(context, R.string.large)
        return arrayOf(small, medium, large)
    }

    fun getTextSize(size: String): Float {
        return when (size) {
            small -> 20f
            medium -> 24f
            large -> 32f
            else -> throw IllegalArgumentException("Invalid : $size")
        }
    }

    fun getTextSize1(size: String): Float {
        return when (size) {
            small -> 24f
            medium -> 30f
            large -> 36f
            else -> throw IllegalArgumentException("Invalid : $size")
        }
    }

    fun getTextSize2(size: String): Float {
        return when (size) {
            small -> 16f
            medium -> 24f
            large -> 30f
            else -> throw IllegalArgumentException("Invalid : $size")
        }
    }

    fun getTextSize3(size: String): Float {
        return when (size) {
            small -> 12f
            medium -> 16f
            large -> 20f
            else -> throw IllegalArgumentException("Invalid : $size")
        }
    }


//    fun getImageSize(size: String):Int {
//        return when (size) {
//            small -> 30
//            medium -> 35
//            large -> 42
//            else -> throw IllegalArgumentException("Invalid : $size")
//        }
//    }
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
    private const val ap = "ap"
    private const val labor = "labor"
    private const val train = "train"
    override val title: Int = R.string.widget_content
    override val key: String = "widget_content"
    override val defaultValue: String = ap
    const val defaultValue2: String = labor
    override fun getEntryValues() = arrayOf(ap, labor, train)

    override fun getEntries(context: Context): Array<String> {
        val ap = getString(context, R.string.ap)
        val labor = getString(context, R.string.labor)
        val train = getString(context, R.string.train)
        return arrayOf(ap, labor, train)
    }
}

data object WidgetUpdateFreq : ListInfo {
    private const val min_15 = "min_15"
    private const val min_30 = "min_30"
    private const val hour_1 = "hour_1"
    override val title: Int = R.string.widget_update_freq
    override val key: String = "widget_update_freq"
    override val defaultValue: String = min_15
    override fun getEntryValues() = arrayOf(min_15, min_30, hour_1)

    override fun getEntries(context: Context): Array<String> {
        val min_15 = getString(context, R.string.min_15)
        val min_30 = getString(context, R.string.min_30)
        val hour_1 = getString(context, R.string.hour_1)
        return arrayOf(min_15, min_30, hour_1)
    }

    fun getValue(str: String): Int {
        return when (str) {
            min_15 -> 900
            min_30 -> 1800
            hour_1 -> 3600
            else -> 1800
        }
    }
}



