package com.blueskybone.arkscreen.data.local.room

import com.blueskybone.arkscreen.data.local.pref.WidgetTemplatePrefManager
import com.blueskybone.arkscreen.data.local.pref.preference.Preference
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy

class WidgetAppearanceTest {
    @Suppress("UNCHECKED_CAST")
    private fun preference(initial: String): Preference<String> {
        var value = initial
        return Proxy.newProxyInstance(Preference::class.java.classLoader, arrayOf(Preference::class.java)) { _, method, args ->
            when (method.name) {
                "get" -> value
                "set" -> { value = args!![0] as String; null }
                else -> error("Unexpected preference call: " + method.name)
            }
        } as Preference<String>
    }
    private fun prefs() = WidgetTemplatePrefManager().apply {
        style = preference("mist")
        layout1x2 = preference("dense")
        layout2x3 = preference("four")
        slot1x1 = preference("DRONE")
        slot1x2 = preference("TRAINING")
        slots2x2 = preference("SANITY,MEETING")
        slots2x3Four = preference("SANITY,DRONE,TRAINING,MEETING")
    }

    @Test fun legacyAppearanceIsCopiedWithoutLosingFixedAccount() {
        val old = WidgetConfig(7, WidgetConfig.FIXED, "arknights", "2", "uid")
        val upgraded = old.withAppearance(prefs())
        assertEquals("mist", upgraded.theme)
        assertEquals("dense", upgraded.layout1x2)
        assertEquals("SANITY,MEETING", upgraded.slots2x2)
        assertEquals(old.roleUid, upgraded.roleUid)
        assertEquals(old.serverKey, upgraded.serverKey)
        assertEquals(1f, upgraded.fontScale, 0f)
        assertEquals(0, upgraded.transparency)
    }

    @Test fun independentWidgetsAndLaterDefaultsNeverOverwriteSavedAppearance() {
        val prefs = prefs()
        val first = WidgetConfig(1).withAppearance(prefs)
            .copy(theme = "rhodes", fontScale = 1.15f, transparency = 60, slots2x2 = "DRONE,TRAINING")
        val second = WidgetConfig(2).withAppearance(prefs)
        prefs.style.set("dark")
        prefs.slots2x2.set("MEETING,RECRUITMENT")
        assertEquals(first, first.withAppearance(prefs))
        assertEquals(second, second.withAppearance(prefs))
        assertEquals("mist", second.theme)
        assertEquals("SANITY,MEETING", second.slots2x2)
        assertEquals("dark", WidgetConfig(3).withAppearance(prefs).theme)
    }
}
