package com.blueskybone.arkscreen.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.receiver.WidgetReceiver.Companion.WORKER_NAME
import com.blueskybone.arkscreen.ui.activity.RealTimeActivity
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetAppearance
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import org.koin.java.KoinJavaComponent
import java.util.concurrent.TimeUnit

/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */

class Widget3 : AppWidgetProvider() {
    private val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray?
    ) {

        val views = RemoteViews(context.packageName, R.layout.widget_3)

        //绑定事件
        val onClickPendingIntent: PendingIntent
        val onClickIntent = Intent(context, RealTimeActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        }
        onClickPendingIntent =
            PendingIntent.getActivity(context, 0, onClickIntent, pendingIntentFlags)
        views.setOnClickPendingIntent(R.id.layout, onClickPendingIntent)

        //layout
        views.setImageViewResource(R.id.widget_skland_bg, R.drawable.widget_background)
        views.setInt(R.id.widget_skland_bg, "setAlpha", prefManager.widgetAlpha.get())
        val colorFilter = if (prefManager.widgetAppearance.get() == WidgetAppearance.whiteOnBlack) {
            Color.BLACK } else { Color.WHITE }
        val textColor = if (prefManager.widgetAppearance.get() == WidgetAppearance.whiteOnBlack) { Color.WHITE
        } else { Color.BLACK }

        views.setInt(R.id.widget_skland_bg, "setColorFilter", colorFilter)
        views.setInt(R.id.value, "setTextColor", textColor)
        views.setInt(R.id.max, "setTextColor", textColor)

        val size = prefManager.widgetContentSize.get()
        views.setTextViewTextSize(
            R.id.value,
            TypedValue.COMPLEX_UNIT_SP,
            WidgetSize.getTextSize(size)
        )
        views.setTextViewTextSize(
            R.id.max,
            TypedValue.COMPLEX_UNIT_SP,
            WidgetSize.getTextSize3(size)
        )

        val now = getCurrentTs()
        val apCache = prefManager.apCache.get()
        val apMax = apCache.max
        val current = if (apCache.current >= apMax ) {
            apCache.current
        } else if(now > apCache.recoverTime) {
            apMax
        }else{
            apMax - (apCache.recoverTime - now).toInt() / (60 * 6) - 1
        }
        views.setTextViewText(R.id.value, "$current")
        views.setTextViewText(R.id.max, "/$apMax")
        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    override fun onEnabled(context: Context?) {
        //创建queue worker
        val workRequest: PeriodicWorkRequest = PeriodicWorkRequest.Builder(
            SklandWorker::class.java,
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS * 2, TimeUnit.MILLISECONDS
        )
            .build()
        WorkManager.getInstance(context!!)
            .enqueueUniquePeriodicWork(
                WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }

    private fun areAllWidgetsEmpty(
        context: Context,
        vararg widgetClasses: Class<out AppWidgetProvider>
    ): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        return widgetClasses.all {
            appWidgetManager.getAppWidgetIds(ComponentName(context, it)).isEmpty()
        }
    }

    override fun onDisabled(context: Context?) {
        if (context != null && areAllWidgetsEmpty(
                context,
                Widget1::class.java,
                Widget2::class.java,
                Widget3::class.java
            )
        ) {
            WorkManager.getInstance(context).cancelUniqueWork(WORKER_NAME)
        }
    }
}