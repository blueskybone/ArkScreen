package com.blueskybone.arkscreen.data.local.pref

import com.blueskybone.arkscreen.data.local.pref.preference.Preference
import com.blueskybone.arkscreen.data.local.pref.preference.PreferenceStore

/** Isolated preferences for the experimental widget template system. */
class WidgetTemplatePrefManager() {
    constructor(store: PreferenceStore) : this() {
        style = store.getString("widget_template_style", STYLE_TRANSPARENT)
        previewSize = store.getString("widget_template_preview_size", SIZE_2X3)
        layout1x2 = store.getString("widget_template_layout_1x2", LAYOUT_SINGLE)
        layout2x3 = store.getString("widget_template_layout_2x3", LAYOUT_FOUR)
        slot1x1 = store.getString("widget_template_slot_1x1", "SANITY")
        slot1x2 = store.getString("widget_template_slot_1x2", "SANITY")
        slots2x2 = store.getString("widget_template_slots_2x2", "SANITY,TRAINING")
        slots2x3Four = store.getString(
            "widget_template_slots_2x3_four",
            "SANITY,DRONE,RECRUITMENT,TRAINING",
        )
    }

    lateinit var style: Preference<String>
    lateinit var previewSize: Preference<String>
    lateinit var layout1x2: Preference<String>
    lateinit var layout2x3: Preference<String>
    lateinit var slot1x1: Preference<String>
    lateinit var slot1x2: Preference<String>
    lateinit var slots2x2: Preference<String>
    lateinit var slots2x3Four: Preference<String>

    companion object {
        const val STYLE_TRANSPARENT = "transparent"
        const val STYLE_DARK = "dark"
        const val STYLE_MIST = "mist"
        const val STYLE_RHODES = "rhodes"
        const val STYLE_SKLAND = "skland"
        const val STYLE_RHINE = "rhine"
        const val STYLE_SUI = "sui"
        const val STYLE_LONETRAIL = "lonetrail"
        const val SIZE_1X1 = "1x1"
        const val SIZE_1X2 = "1x2"
        const val SIZE_2X2 = "2x2"
        const val SIZE_2X3 = "2x3"
        const val LAYOUT_SINGLE = "single"
        const val LAYOUT_DENSE = "dense"
        const val LAYOUT_FULL = "full"
        const val LAYOUT_FOUR = "four"
    }
}
