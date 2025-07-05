package com.blueskybone.arkscreen.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetAppearance
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetTextColor
import org.koin.java.KoinJavaComponent

import java.util.concurrent.TimeUnit

/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */
class AttendanceWidget : AppWidgetProvider() {
    private val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()

    companion object {
        const val WORKER_NAME = "AttendanceWorker"
        const val ACTION_ANIMATE = "com.blueskybone.arkscreen.ACTION_ANIMATE"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        appWidgetIds.forEach { appWidgetId ->
            super.onUpdate(context, appWidgetManager, appWidgetIds)
            val views = RemoteViews(context.packageName, R.layout.widget_attendance)


            views.setImageViewResource(R.id.widget_skland_bg, prefManager.widgetBg.get())
//
            views.setInt(R.id.widget_skland_bg, "setAlpha", prefManager.widgetAlpha.get())
            val textColor = WidgetTextColor.getColorInt(prefManager.widgetTextColor.get())
            println("textColor ${prefManager.widgetTextColor.get()} $textColor")

            views.setInt(R.id.recruit, "setTextColor", textColor)
            views.setInt(R.id.refresh, "setTextColor", textColor)

            views.setInt(R.id.ap_current, "setTextColor", textColor)
            views.setInt(R.id.ap_max, "setTextColor", textColor)
            views.setInt(R.id.ap_resc, "setTextColor", textColor)
            views.setImageViewResource(R.id.ic_bolt, R.drawable.ic_bolt)
            views.setInt(R.id.ic_bolt, "setColorFilter", textColor)

            views.setInt(R.id.labor_current, "setTextColor", textColor)
            views.setInt(R.id.labor_max, "setTextColor", textColor)
            views.setInt(R.id.labor_resc, "setTextColor", textColor)
            views.setImageViewResource(R.id.ic_labor, R.drawable.ic_drone)
            views.setInt(R.id.ic_labor, "setColorFilter", textColor)

            views.setInt(R.id.train, "setTextColor", textColor)
            views.setInt(R.id.train_name, "setTextColor", textColor)
            views.setInt(R.id.train_resc, "setTextColor", textColor)
            views.setImageViewResource(R.id.ic_train, R.drawable.ic_train)
            views.setInt(R.id.ic_train, "setColorFilter", textColor)


            // 设置点击意图
            val clickIntent = Intent(context, AttendanceWidget::class.java).apply {
                action = ACTION_ANIMATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.loading, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
//            appWidgetManager.updateAppWidget(appWidgetIds, views)
        }
    }

    override fun onEnabled(context: Context?) {
//        val workRequest: PeriodicWorkRequest = PeriodicWorkRequest.Builder(
//            AttendanceWorker::class.java,
//            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS * 2, TimeUnit.MILLISECONDS
//        ).build()
//        WorkManager.getInstance(context!!)
//            .enqueueUniquePeriodicWork(
//                WORKER_NAME,
//                ExistingPeriodicWorkPolicy.KEEP,
//                workRequest
//            )
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context?) {
        WorkManager.getInstance(context!!).cancelUniqueWork(WORKER_NAME)
        super.onDisabled(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_ANIMATE) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                playFadeOutAnimation(context, appWidgetId)
            }
        }
    }

    private fun playFadeOutAnimation(context: Context, appWidgetId: Int) {


//        val views = RemoteViews(context.packageName, R.layout.widget_attendance).apply {
//            setImageViewResource(R.id.loading, R.drawable.click_anim)
//        }
//
//        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)
//
//        // 启动动画
//        views.setImageViewResource(R.id.loading, R.drawable.click_anim)
//        AppWidgetManager.getInstance(context).partiallyUpdateAppWidget(appWidgetId, views)


//        val appWidgetManager = AppWidgetManager.getInstance(context)
//
//        // 1. 显示视图 (初始alpha=0.8)
//        val showViews = RemoteViews(context.packageName, R.layout.widget_attendance).apply {
//            setViewVisibility(R.id.loading, View.VISIBLE)
//            setFloat(R.id.loading, "setAlpha", 0.8f)
//        }
//        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, showViews)
//
//        // 2. 渐隐动画 (分5步)
//        for (i in 1..5) {
//            Handler(Looper.getMainLooper()).postDelayed({
//                val animatedViews =
//                    RemoteViews(context.packageName, R.layout.widget_attendance).apply {
//                        // 计算当前alpha (从0.8线性递减到0)
//                        val alpha = 0.8f - (0.8f * i / 5f)
//                        setFloat(R.id.loading, "setAlpha", alpha)
//
//                        // 最后一步隐藏视图
//                        if (i == 5) {
//                            setViewVisibility(R.id.loading, View.GONE)
//                        }
//                    }
//                appWidgetManager.partiallyUpdateAppWidget(appWidgetId, animatedViews)
//            }, (i * 80).toLong()) // 每80毫秒一帧，总共400毫秒动画
//        }
    }

}