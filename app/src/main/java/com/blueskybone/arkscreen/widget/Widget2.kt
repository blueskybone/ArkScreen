package com.blueskybone.arkscreen.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.activity.RealTimeActivity
import com.blueskybone.arkscreen.bindinginfo.WidgetAppearance
import com.blueskybone.arkscreen.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.receiver.WidgetReceiver.Companion.WORKER_NAME
import com.blueskybone.arkscreen.util.TimeUtils
import com.hjq.toast.Toaster
import org.koin.java.KoinJavaComponent
import java.util.concurrent.TimeUnit

/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */


class Widget2 : AppWidgetProvider() {

    private val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray?
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_2)
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

        val backgroundColor: Int
        val textColor: Int
        val boltIcon: Int
        val droneIcon: Int

        when (prefManager.widgetAppearance.get()) {
            WidgetAppearance.whiteOnBlack -> {
                backgroundColor = Color.BLACK
                textColor = Color.WHITE
                boltIcon = R.drawable.ic_bolt
                droneIcon = R.drawable.ic_drone
            }
            else -> {
                backgroundColor = Color.WHITE
                textColor = Color.BLACK
                boltIcon = R.drawable.ic_bolt_black
                droneIcon = R.drawable.ic_drone_black
            }
        }

        views.setInt(R.id.widget_skland_bg, "setColorFilter", backgroundColor)
        views.setInt(R.id.widget_text_ap, "setTextColor", textColor)
        views.setInt(R.id.widget_text_labor, "setTextColor", textColor)
        views.setInt(R.id.widget_text_ap_rest, "setTextColor", textColor)
        views.setInt(R.id.widget_text_labor_rest, "setTextColor", textColor)

        views.setImageViewResource(R.id.ic_bolt, boltIcon)
        views.setImageViewResource(R.id.ic_drone, droneIcon)

        val size = prefManager.widgetContentSize.get()

        fun setTextViewSize(viewId: Int, size: String, isPrimary: Boolean) {
            val textSize = if (isPrimary) {
                WidgetSize.getTextSize(size)
            } else {
                WidgetSize.getTextSize3(size)
            }
            views.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_SP, textSize)
        }
        setTextViewSize(R.id.widget_text_ap, size, true)
        setTextViewSize(R.id.widget_text_labor, size, true)
        setTextViewSize(R.id.widget_text_ap_rest, size, false)
        setTextViewSize(R.id.widget_text_labor_rest, size, false)

        val apCache = prefManager.apCache.get()
        val laborCache = prefManager.laborCache.get()
        views.setTextViewText(R.id.widget_text_ap, "${apCache.current} / ${apCache.max}")
        views.setTextViewText(
            R.id.widget_text_labor,
            "${laborCache.current} / ${laborCache.max}"
        )
        views.setTextViewText(
            R.id.widget_text_ap_rest,
            TimeUtils.getRemainTimeMinStr(apCache.remainSec)
        )
        views.setTextViewText(
            R.id.widget_text_labor_rest,
            TimeUtils.getRemainTimeMinStr(laborCache.remainSec)
        )

        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    override fun onEnabled(context: Context?) {
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