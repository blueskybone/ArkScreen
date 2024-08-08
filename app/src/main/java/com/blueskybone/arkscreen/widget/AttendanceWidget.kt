package com.blueskybone.arkscreen.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blueskybone.arkscreen.R

import java.util.concurrent.TimeUnit

/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */
class AttendanceWidget : AppWidgetProvider() {

    private val WORKER_NAME = "AttendanceWorker"
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_attendance)
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context?) {
        val workRequest: PeriodicWorkRequest = PeriodicWorkRequest.Builder(
            AttendanceWorker::class.java,
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS * 2, TimeUnit.MILLISECONDS
        ).build()
        WorkManager.getInstance(context!!)
            .enqueueUniquePeriodicWork(
                this.WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

        super.onEnabled(context)
    }

    override fun onDisabled(context: Context?) {
        WorkManager.getInstance(context!!).cancelUniqueWork(this.WORKER_NAME)
        super.onDisabled(context)
    }
}