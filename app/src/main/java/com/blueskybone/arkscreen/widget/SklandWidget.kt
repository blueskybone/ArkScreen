package com.blueskybone.arkscreen.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.activity.RealTimeActivity
import com.hjq.toast.Toaster
import java.util.concurrent.TimeUnit

/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */
class SklandWidget : AppWidgetProvider() {

//    val APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE"
    val MANUAL_UPDATE = "MANUAL_UPDATE"
    private val WORKER_NAME = "SklandWorker"
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray?
    ) {
        val intent = Intent()
        intent.setClass(context, SklandWidget::class.java)
        intent.action = this.MANUAL_UPDATE
        //绑定事件
        val pendingIntent: PendingIntent
        val onClickPendingIntent: PendingIntent
        val onClickIntent = Intent(context, RealTimeActivity::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
            onClickPendingIntent =
                PendingIntent.getActivity(context, 0, onClickIntent, PendingIntent.FLAG_MUTABLE)
        } else {
            pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            onClickPendingIntent = PendingIntent.getActivity(
                context,
                0,
                onClickIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val remoteViews = RemoteViews(context.packageName, R.layout.skland_widget)
        remoteViews.setOnClickPendingIntent(R.id.refresh, pendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.widget_text, onClickPendingIntent)
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
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
                this.WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }

    override fun onDisabled(context: Context?) {
        WorkManager.getInstance(context!!).cancelUniqueWork(this.WORKER_NAME)
    }

    override fun onReceive(context: Context?, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == this.MANUAL_UPDATE) {
            Toaster.show("刷新中...")
            WorkManager.getInstance(context!!)
                .enqueue(OneTimeWorkRequest.from(SklandWorker::class.java))
        }
    }
}