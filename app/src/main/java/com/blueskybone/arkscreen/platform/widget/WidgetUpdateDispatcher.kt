package com.blueskybone.arkscreen.platform.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.blueskybone.arkscreen.ui.widget.WidgetNext1
import com.blueskybone.arkscreen.ui.widget.WidgetNext2
import com.blueskybone.arkscreen.ui.widget.WidgetNext3
import com.blueskybone.arkscreen.ui.widget.WidgetNext4
import timber.log.Timber

/** 使用最新本地缓存重绘所有已安装的桌面组件。 */
class WidgetUpdateDispatcher(context: Context) {
    private val appContext = context.applicationContext

    fun renderAll() {
        val manager = AppWidgetManager.getInstance(appContext)
        var widgetCount = 0
        PROVIDERS.forEach { provider ->
            val component = ComponentName(appContext, provider)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                widgetCount += ids.size
                appContext.sendBroadcast(
                    Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        .setComponent(component)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                )
            }
        }
        Timber.tag("Widget").i("Render dispatched: widgetCount=%d", widgetCount)
    }

    fun hasWidgets(): Boolean {
        val manager = AppWidgetManager.getInstance(appContext)
        return PROVIDERS.any { provider ->
            manager.getAppWidgetIds(ComponentName(appContext, provider)).isNotEmpty()
        }
    }

    companion object {
        val PROVIDERS = listOf(
            WidgetNext1::class.java,
            WidgetNext2::class.java,
            WidgetNext3::class.java,
            WidgetNext4::class.java,
        )
    }
}
