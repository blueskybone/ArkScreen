package com.blueskybone.arkscreen.domain.model.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingOptionsTest {
    @Test fun preservesExistingStoredKeysValuesAndDefaults() {
        assertEquals("app_theme", AppThemeSetting.key)
        assertEquals(listOf("light", "dark", "system"), AppThemeSetting.values)
        assertEquals("system", AppThemeSetting.defaultValue)
        assertEquals("recruit_mode", RecruitModeSetting.key)
        assertEquals(listOf("floatWindow", "toast", "Auto"), RecruitModeSetting.values)
        assertEquals("floatWindow", RecruitModeSetting.defaultValue)
        assertEquals("float_window_appearance", FloatWindowAppearanceSetting.key)
        assertEquals(listOf("simple", "colorful"), FloatWindowAppearanceSetting.values)
        assertEquals("colorful", FloatWindowAppearanceSetting.defaultValue)
        assertEquals("screenshot_delay", ScreenshotDelaySetting.key)
        assertEquals(listOf("1000", "2000", "3000"), ScreenshotDelaySetting.values)
        assertEquals("1000", ScreenshotDelaySetting.defaultValue)
        assertEquals("widget_update_freq", WidgetUpdateFrequencySetting.key)
        assertEquals(listOf("min_15", "min_30", "hour_1"), WidgetUpdateFrequencySetting.values)
        assertEquals("min_15", WidgetUpdateFrequencySetting.defaultValue)
    }

    @Test fun preservesTimingUnitsAndUnknownFrequencyFallback() {
        assertEquals(listOf(1000L, 2000L, 3000L), ScreenshotDelaySetting.values.map(ScreenshotDelaySetting::getMSec))
        assertEquals(listOf(900, 1800, 3600), WidgetUpdateFrequencySetting.values.map(WidgetUpdateFrequencySetting::getValue))
        assertEquals(1800, WidgetUpdateFrequencySetting.getValue("legacy"))
    }
}
