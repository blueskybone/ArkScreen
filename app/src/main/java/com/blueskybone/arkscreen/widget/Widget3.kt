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
import com.blueskybone.arkscreen.receiver.WidgetReceiver.Companion.WORKER_NAME
import com.blueskybone.arkscreen.ui.activity.RealTimeActivity
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

class Widget3 : AppWidgetProvider() {
    private val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray?
    ) {

        val views = RemoteViews(context.packageName, R.layout.widget_2x2)

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

        //设置外观
        views.setImageViewResource(R.id.widget_bg, prefManager.widgetBg.get())
        views.setInt(R.id.widget_bg, "setAlpha", prefManager.widgetAlpha.get())

        //textColor
        //content1
        val textColor = WidgetTextColor.getColorInt(prefManager.widgetTextColor.get())
        views.setInt(R.id.text_1, "setTextColor", textColor)
        views.setInt(R.id.rest_1, "setTextColor", textColor)
        val icon1 = WidgetContent.getDrawableIcon(prefManager.widget3Content1.get())
        views.setImageViewResource(R.id.icon_1, icon1)
        views.setInt(R.id.icon_1, "setColorFilter", textColor)
        //content2
        views.setInt(R.id.text_2, "setTextColor", textColor)
        views.setInt(R.id.rest_2, "setTextColor", textColor)
        val icon2 = WidgetContent.getDrawableIcon(prefManager.widget3Content2.get())
        views.setImageViewResource(R.id.icon_2, icon2)
        views.setInt(R.id.icon_2, "setColorFilter", textColor)

        //设置size
        val mainSize = WidgetSize.getTextSizeMain(prefManager.widget3Size.get())
        val subSize = WidgetSize.getTextSizeSub(prefManager.widget3Size.get())
        val imageSize = WidgetSize.getImageSize(prefManager.widget3Size.get())
        val spType = TypedValue.COMPLEX_UNIT_SP
        val dpType = TypedValue.COMPLEX_UNIT_DIP

        views.apply {
            setTextViewTextSize(R.id.text_1, spType, mainSize)
            setTextViewTextSize(R.id.rest_1, spType, subSize)
            setTextViewTextSize(R.id.text_2, spType, mainSize)
            setTextViewTextSize(R.id.rest_2, spType, subSize)
            setViewLayoutHeight(R.id.icon_1, imageSize.toFloat(), dpType)
            setViewLayoutWidth(R.id.icon_1, imageSize.toFloat(), dpType)
            setViewLayoutHeight(R.id.icon_2, imageSize.toFloat(), dpType)
            setViewLayoutWidth(R.id.icon_2, imageSize.toFloat(), dpType)
        }
        updateWidgetContent(
            prefManager.widget3Content1.get(),
            R.id.text_1,
            R.id.rest_1,
            views
        )
        updateWidgetContent(
            prefManager.widget3Content2.get(),
            R.id.text_2,
            R.id.rest_2,  // 注意：原代码中第二个restView应该是R.id.rest_2而不是R.id.text_2
            views
        )

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