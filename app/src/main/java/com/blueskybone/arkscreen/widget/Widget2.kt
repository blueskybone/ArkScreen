package com.blueskybone.arkscreen.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.preference.PrefManager
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


class Widget2 : AppWidgetProvider() {

    private val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray?
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_1x2)
        //绑定事件
//        val onClickPendingIntent: PendingIntent
//        val onClickIntent = Intent(context, RealTimeActivity::class.java)
//        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            PendingIntent.FLAG_MUTABLE
//        } else {
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        }
//        onClickPendingIntent =
//            PendingIntent.getActivity(context, 0, onClickIntent, pendingIntentFlags)
//        views.setOnClickPendingIntent(R.id.layout, onClickPendingIntent)

//layout
        views.setImageViewResource(R.id.widget_bg, prefManager.widgetBg.get())
        views.setInt(R.id.widget_bg, "setAlpha", prefManager.widgetAlpha.get())

        //textColor
        val textColor = WidgetTextColor.getColorInt(prefManager.widgetTextColor.get())
        views.setInt(R.id.text, "setTextColor", textColor)
        views.setInt(R.id.rest, "setTextColor", textColor)

        val drawable = WidgetContent.getDrawableIcon(prefManager.widget2Content.get())
        views.setImageViewResource(R.id.icon, drawable)
        views.setInt(R.id.icon, "setColorFilter", textColor)

        //content
        updateWidgetContent(prefManager.widget2Content.get(), R.id.text, R.id.rest, views)
        //Size
        val size = prefManager.widget2Size.get()
        views.setTextViewTextSize(
            R.id.text,
            TypedValue.COMPLEX_UNIT_SP,
            WidgetSize.getTextSizeMain(size)
        )
        views.setTextViewTextSize(
            R.id.rest,
            TypedValue.COMPLEX_UNIT_SP,
            WidgetSize.getTextSizeSub(size)
        )
        views.setViewLayoutHeight(
            R.id.icon,
            WidgetSize.getImageSize(size).toFloat(),
            TypedValue.COMPLEX_UNIT_DIP
        )
        views.setViewLayoutWidth(
            R.id.icon,
            WidgetSize.getImageSize(size).toFloat(),
            TypedValue.COMPLEX_UNIT_DIP
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

    private fun updateWidgetContent(
        contentPref: String,
        textViewId: Int,
        restViewId: Int,
        views: RemoteViews
    ) {
        when (contentPref) {
            "ap" -> {
                fun Long.toMinutes() = this / 60

                val now = getCurrentTs()
                val apCache = prefManager.apCache.get()
                val apMax = apCache.max

                val current = when {
                    apCache.current >= apMax -> apCache.current
                    now > apCache.recoverTime -> apMax
                    else -> apMax - (apCache.recoverTime - now).toMinutes() / 6 - 1
                }
                views.apply {
                    setTextViewText(textViewId, "$current / $apMax")
                    setTextViewText(
                        restViewId,
                        TimeUtils.getRemainTimeMinStr(apCache.recoverTime - now)
                    )
                }
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
                views.setTextViewText(textViewId, "$curr / $max")
                views.setTextViewText(
                    restViewId,
                    TimeUtils.getRemainTimeMinStr(laborCache.remainSec - now + laborCache.lastSyncTs)
                )
            }

            "train" -> {
                val now = getCurrentTs()
                val trainCache = prefManager.trainCache.get()
                if (trainCache.isnull) {
                    views.setTextViewText(textViewId, "暂无数据")
                } else {
                    when (trainCache.status) {
                        -1L -> {
                            views.setTextViewText(textViewId, "空闲中")
                            views.setTextViewText(restViewId, "idle")
                        }

                        0L -> {
                            views.setTextViewText(textViewId, trainCache.trainee)
                            views.setTextViewText(restViewId, "completed")
                        }

                        1L -> {
                            views.setTextViewText(textViewId, trainCache.trainee)
                            if (now > trainCache.completeTime) {
                                views.setTextViewText(restViewId, "completed")
                            } else {
                                views.setTextViewText(
                                    restViewId,
                                    TimeUtils.getRemainTimeMinStr(now - trainCache.completeTime)
                                )
                            }
                        }

                        else -> {
                            views.setTextViewText(textViewId, "status错误")
                            Timber.e("trainCache.status ${trainCache.status}")
                        }
                    }
                }
            }

            else -> {
                Timber.e("Unknown contentPref: $contentPref")
            }
        }
    }
}