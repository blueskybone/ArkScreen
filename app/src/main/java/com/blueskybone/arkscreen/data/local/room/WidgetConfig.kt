package com.blueskybone.arkscreen.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WidgetConfig(
    @PrimaryKey val appWidgetId: Int,
    val accountMode: String = FOLLOW_CURRENT,
    val game: String = "arknights",
    val serverKey: String? = null,
    val roleUid: String? = null,
    val theme: String? = null,
    val layout1x2: String? = null,
    val layout2x3: String? = null,
    val slot1x1: String? = null,
    val slot1x2: String? = null,
    val slots2x2: String? = null,
    val slots2x3: String? = null,
    @androidx.room.ColumnInfo(defaultValue = "1.0") val fontScale: Float = 1f,
    @androidx.room.ColumnInfo(defaultValue = "0") val transparency: Int = 0,
) {
    /** Snapshot the legacy global appearance on first use. */
    fun withAppearance(p: com.blueskybone.arkscreen.data.local.pref.WidgetTemplatePrefManager) = copy(
        theme = theme ?: p.style.get(),
        layout1x2 = layout1x2 ?: p.layout1x2.get(),
        layout2x3 = layout2x3 ?: p.layout2x3.get(),
        slot1x1 = slot1x1 ?: p.slot1x1.get(),
        slot1x2 = slot1x2 ?: p.slot1x2.get(),
        slots2x2 = slots2x2 ?: p.slots2x2.get(),
        slots2x3 = slots2x3 ?: p.slots2x3Four.get(),
    )

    companion object {
        const val FOLLOW_CURRENT = "FOLLOW_CURRENT"
        const val FIXED = "FIXED"
    }
}
