package com.blueskybone.arkscreen.platform.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.blueskybone.arkscreen.ui.widget.Widget1
import com.blueskybone.arkscreen.ui.widget.Widget2
import com.blueskybone.arkscreen.ui.widget.Widget3
import com.blueskybone.arkscreen.ui.widget.Widget4
import com.blueskybone.arkscreen.ui.widget.WidgetNext1
import com.blueskybone.arkscreen.ui.widget.WidgetNext2
import com.blueskybone.arkscreen.ui.widget.WidgetNext3
import com.blueskybone.arkscreen.ui.widget.WidgetNext4

/** Redraws every installed widget from the latest local cache. */
class WidgetUpdateDispatcher(context: Context) {
    private val appContext = context.applicationContext

    fun renderAll() {
        val manager = AppWidgetManager.getInstance(appContext)
        PROVIDERS.forEach { provider ->
            val component = ComponentName(appContext, provider)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                appContext.sendBroadcast(
                    Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        .setComponent(component)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                )
            }
        }
    }

    fun hasWidgets(): Boolean {
        val manager = AppWidgetManager.getInstance(appContext)
        return PROVIDERS.any { provider ->
            manager.getAppWidgetIds(ComponentName(appContext, provider)).isNotEmpty()
        }
    }

    companion object {
        val PROVIDERS = listOf(
            Widget1::class.java,
            Widget2::class.java,
            Widget3::class.java,
            Widget4::class.java,
            WidgetNext1::class.java,
            WidgetNext2::class.java,
            WidgetNext3::class.java,
            WidgetNext4::class.java,
        )
    }
}
