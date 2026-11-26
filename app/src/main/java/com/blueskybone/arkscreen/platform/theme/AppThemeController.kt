package com.blueskybone.arkscreen.platform.theme

import com.blueskybone.arkscreen.domain.model.settings.AppThemeSetting
import androidx.appcompat.app.AppCompatDelegate
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager

/** 将持久化的主题选择同时应用到 AppCompat 和当前活动的 Activity。 */
class AppThemeController(
    private val settings: SettingPrefManager,
) {
    fun applySavedTheme() {
        val mode = when (settings.appTheme.get()) {
            AppThemeSetting.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppThemeSetting.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}
