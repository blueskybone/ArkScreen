package com.blueskybone.arkscreen.ui.common.bindinginfo

import android.content.Context
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.domain.model.settings.*

/** Labels for preference controls; persisted values are defined in Domain. */
sealed interface ListInfo {
    val title: Int
    val defaultValue: String
    fun getEntryValues(): Array<String>
    fun getEntries(context: Context): Array<String>
}

data object FloatWindowAppearance : ListInfo {
    override val title = R.string.float_window_appearance
    override val defaultValue = FloatWindowAppearanceSetting.defaultValue
    override fun getEntryValues() = FloatWindowAppearanceSetting.values.toTypedArray()
    override fun getEntries(context: Context) = arrayOf(context.getString(R.string.simple), context.getString(R.string.colorful))
}

data object ScDelay : ListInfo {
    override val title = R.string.screenshot_delay
    override val defaultValue = ScreenshotDelaySetting.defaultValue
    override fun getEntryValues() = ScreenshotDelaySetting.values.toTypedArray()
    override fun getEntries(context: Context) = arrayOf(context.getString(R.string.sec_1), context.getString(R.string.sec_2), context.getString(R.string.sec_3))
}

data object AppTheme : ListInfo {
    override val title = R.string.app_theme
    override val defaultValue = AppThemeSetting.defaultValue
    override fun getEntryValues() = AppThemeSetting.values.toTypedArray()
    override fun getEntries(context: Context) = arrayOf(context.getString(R.string.theme_light), context.getString(R.string.theme_dark), context.getString(R.string.theme_system))
}

data object RecruitMode : ListInfo {
    override val title = R.string.recruit_show_mode
    override val defaultValue = RecruitModeSetting.defaultValue
    override fun getEntryValues() = RecruitModeSetting.values.toTypedArray()
    override fun getEntries(context: Context) = arrayOf(context.getString(R.string.float_win), context.getString(R.string.toast), context.getString(R.string.auto))
}

data object WidgetUpdateFreq : ListInfo {
    override val title = R.string.widget_update_freq
    override val defaultValue = WidgetUpdateFrequencySetting.defaultValue
    override fun getEntryValues() = WidgetUpdateFrequencySetting.values.toTypedArray()
    override fun getEntries(context: Context) = arrayOf(context.getString(R.string.min_15), context.getString(R.string.min_30), context.getString(R.string.hour_1))
}
