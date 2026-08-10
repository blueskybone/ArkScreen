package com.blueskybone.arkscreen.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.WorkManager
import com.blueskybone.arkscreen.ui.widget.WidgetReceiver.Companion.WORKER_NAME

object WidgetWorkScheduler {
    private val providers = listOf(
        Widget1::class.java,
        Widget2::class.java,
        Widget3::class.java,
        Widget4::class.java
    )

    fun cancelIfNoWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val hasWidgets = providers.any { provider ->
            manager.getAppWidgetIds(ComponentName(context, provider)).isNotEmpty()
        }
        if (!hasWidgets) WorkManager.getInstance(context).cancelUniqueWork(WORKER_NAME)
    }
}
