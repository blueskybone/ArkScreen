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
) {
    companion object {
        const val FOLLOW_CURRENT = "FOLLOW_CURRENT"
        const val FIXED = "FIXED"
    }
}
