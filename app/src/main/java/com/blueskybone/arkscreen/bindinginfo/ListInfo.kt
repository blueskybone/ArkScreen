package com.blueskybone.arkscreen.bindinginfo

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
