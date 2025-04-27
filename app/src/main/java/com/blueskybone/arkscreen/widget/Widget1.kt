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
import com.hjq.toast.Toaster
import org.koin.java.KoinJavaComponent
import java.util.concurrent.TimeUnit

/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */


class Widget1 : AppWidgetProvider() {

    private val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray?
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_1)

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

        when (prefManager.widgetAppearance.get()) {
            WidgetAppearance.whiteOnBlack -> {
                backgroundColor = Color.BLACK
                textColor = Color.WHITE
                boltIcon = R.drawable.ic_bolt
            }
            else -> {
                backgroundColor = Color.WHITE
                textColor = Color.BLACK
                boltIcon = R.drawable.ic_bolt_black
            }
        }

        views.setInt(R.id.widget_skland_bg, "setColorFilter", backgroundColor)
        views.setInt(R.id.widget_text, "setTextColor", textColor)
        views.setImageViewResource(R.id.ic_bolt, boltIcon)

        val size = prefManager.widgetContentSize.get()
        views.setTextViewTextSize(
            R.id.widget_text,
            TypedValue.COMPLEX_UNIT_SP,
            WidgetSize.getTextSize(size)
        )
        val apCache = prefManager.apCache.get()
        views.setTextViewText(R.id.widget_text, "${apCache.current} / ${apCache.max}")
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

//    override fun onAppWidgetOptionsChanged(
//        context: Context,
//        appWidgetManager: AppWidgetManager,
//        appWidgetId: Int,
//        newOptions: Bundle
//    ){
//        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
//        // 获取小部件的最小宽度和高度
//        val minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
//        val minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
//
//        // 根据大小调整布局
//        //updateWidgetLayout(context, appWidgetManager, appWidgetId, minWidth, minHeight)
//    }

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

//    override fun onReceive(context: Context?, intent: Intent) {
//        super.onReceive(context, intent)
//        if (intent.action == MANUAL_UPDATE) {
//            Toaster.show("小部件刷新中...")
//            WorkManager.getInstance(context!!)
//                .enqueue(OneTimeWorkRequest.from(SklandWorker::class.java))
//        }
//    }

//    private fun getWidgetSize(minWidth: Int, minHeight: Int): Pair<Int, Int> {
//        val cellWidth = 70 // 每个网格单元的宽度（dp）
//        val cellHeight = 70 // 每个网格单元的高度（dp）
//
//        val widthCells = (minWidth + cellWidth - 1) / cellWidth
//        val heightCells = (minHeight + cellHeight - 1) / cellHeight
//
//        return Pair(widthCells, heightCells)
//    }
}