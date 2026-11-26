package com.blueskybone.arkscreen.domain.model.widget

data class WidgetConfig(
    val appWidgetId: Int,
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
    val fontScale: Float = 1f,
    val transparency: Int = 0,
) {
    companion object {
        const val FOLLOW_CURRENT = "FOLLOW_CURRENT"
        const val FIXED = "FIXED"
    }
}
