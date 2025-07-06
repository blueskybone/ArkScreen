package com.blueskybone.arkscreen.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.receiver.WidgetReceiver
import com.blueskybone.arkscreen.receiver.WidgetReceiver.Companion.MANUAL_UPDATE
import com.blueskybone.arkscreen.receiver.WidgetReceiver.Companion.WORKER_NAME
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetContent
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetTextColor
import com.blueskybone.arkscreen.util.TimeUtils
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */


class Widget1 : AppWidgetProvider() {

    private val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()

    companion object {
        const val REQUEST_CODE = 1097
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_1x1)
            views.setImageViewResource(R.id.widget_bg, R.drawable.widget_bg_black)
            views.setInt(R.id.widget_bg, "setAlpha", prefManager.widgetAlpha.get())

            //textColor
            val textColor = WidgetTextColor.getColorInt(prefManager.widgetTextColor.get())
            views.setInt(R.id.value, "setTextColor", textColor)
            views.setInt(R.id.max, "setTextColor", textColor)

            //content
            val content = prefManager.widget1Content.get()
            val drawable = WidgetContent.getDrawableIcon(content)
            views.setImageViewResource(R.id.icon, drawable)
            views.setInt(R.id.icon, "setColorFilter", textColor)

            when (content) {
                "ap" -> {
                    val now = getCurrentTs()
                    val apCache = prefManager.apCache.get()
                    val apMax = apCache.max
                    val current = if (apCache.current >= apMax) {
                        apCache.current
                    } else if (now > apCache.recoverTime) {
                        apMax
                    } else {
                        apMax - (apCache.recoverTime - now).toInt() / (60 * 6) - 1
                    }
                    views.setTextViewText(R.id.value, "$current")
                    views.setTextViewText(R.id.max, "$apMax")
                }

                "labor" -> {
                    val now = getCurrentTs()
                    val laborCache = prefManager.laborCache.get()
                    val max = laborCache.max
                    val curr = run {
                        if (laborCache.remainSec == 0L) {
                            laborCache.max
                        } else {
                            val progress =
                                (now - laborCache.lastSyncTs) * (laborCache.max - laborCache.current)
                            val calculated =
                                ((progress / laborCache.remainSec) + laborCache.current).toInt()
                            calculated.coerceAtMost(laborCache.max)
                        }
                    }
                    views.setTextViewText(R.id.value, "$curr")
                    views.setTextViewText(R.id.max, "$max")
                }

                "train" -> {
                    val now = getCurrentTs()
                    val trainCache = prefManager.trainCache.get()
                    if (trainCache.isnull) {
                        views.setTextViewText(R.id.value, "暂无数据")
                    } else {
                        when (trainCache.status) {
                            -1L -> {
                                views.setTextViewText(R.id.value, "空闲中")
                                views.setTextViewText(R.id.max, "idle")
                            }

                            0L -> {
                                views.setTextViewText(R.id.value, trainCache.trainee)
                                views.setTextViewText(R.id.max, "completed")
                            }

                            1L -> {
                                views.setTextViewText(R.id.value, trainCache.trainee)
                                if (now > trainCache.completeTime) {
                                    views.setTextViewText(R.id.max, "completed")
                                } else {
                                    views.setTextViewText(
                                        R.id.max,
                                        TimeUtils.getRemainTimeMinStr(now - trainCache.completeTime)
                                    )
                                }
                            }

                            else -> {
                                views.setTextViewText(R.id.value, "status错误")
                                Timber.e("trainCache.status ${trainCache.status}")
                            }
                        }
                    }
                }

                else -> {
                    Timber.e("prefManager.widget1Content.get() ${prefManager.widget1Content.get()}")
                }
            }

            //Size
            val size = prefManager.widget1Size.get()
            views.setTextViewTextSize(
                R.id.value,
                TypedValue.COMPLEX_UNIT_SP,
                WidgetSize.getTextSizeMain(size)
            )
            views.setTextViewTextSize(
                R.id.max,
                TypedValue.COMPLEX_UNIT_SP,
                WidgetSize.getTextSizeSub(size)
            )

            // 创建 PendingIntent 发送广播
            val intent = Intent(context, WidgetReceiver::class.java).apply {
                action = MANUAL_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId, // 使用 widgetId 作为 requestCode 确保唯一性
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.layout, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

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