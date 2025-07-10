package com.blueskybone.arkscreen.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
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
import com.blueskybone.arkscreen.util.dpToPx
import com.blueskybone.arkscreen.util.getTargetDrawableId
import org.koin.java.KoinJavaComponent
import timber.log.Timber
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
        appWidgetIds: IntArray
    ) {

        appWidgetIds.forEach { appWidgetId ->


            val views = RemoteViews(context.packageName, R.layout.widget_1x2)


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
            val mainSize = WidgetSize.getTextSizeMain(prefManager.widget2Size.get())
            val subSize = WidgetSize.getTextSizeSub(prefManager.widget2Size.get())
            val iconSize = WidgetSize.getIconSize(prefManager.widget2Size.get())
            val spType = TypedValue.COMPLEX_UNIT_SP
            val dpType = TypedValue.COMPLEX_UNIT_DIP

            views.setTextViewTextSize(R.id.text, spType, mainSize)
            views.setTextViewTextSize(R.id.rest, spType, subSize)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setViewLayoutHeight(R.id.icon, iconSize.toFloat(), dpType)
                views.setViewLayoutWidth(R.id.icon, iconSize.toFloat(), dpType)
            } else {
                val size = dpToPx(iconSize)
                val bitmap = ResourcesCompat.getDrawable(
                    context.resources,
                    getTargetDrawableId(drawable, prefManager.widgetTextColor),
                    null
                )?.toBitmap()!!
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
                views.setImageViewBitmap(R.id.icon, scaledBitmap)
            }

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

//        val intent = Intent(context, WidgetReceiver::class.java).apply {
//            action = "com.yourpackage.ACTION_WIDGET_CLICK"
//            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, REQUEST_CODE)
//        }
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            REQUEST_CODE, // 使用 widgetId 作为 requestCode 确保唯一性
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//        views.setOnClickPendingIntent(R.id.layout, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
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