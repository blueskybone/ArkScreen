package com.blueskybone.arkscreen.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.TypedValue
import android.view.View
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
class Widget4 : AppWidgetProvider() {
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
            val views = RemoteViews(context.packageName, R.layout.widget_2x3)
            //设置color
            views.apply {
                setImageViewResource(R.id.widget_bg, prefManager.widgetBg.get())
                setInt(R.id.widget_bg, "setAlpha", prefManager.widgetAlpha.get())
            }
            val textColor = WidgetTextColor.getColorInt(prefManager.widgetTextColor.get())
            views.apply {
                setInt(R.id.recruit, "setTextColor", textColor)
                setInt(R.id.refresh, "setTextColor", textColor)
            }
            views.apply {
                setInt(R.id.ap_current, "setTextColor", textColor)
                setInt(R.id.ap_max, "setTextColor", textColor)
                setInt(R.id.ap_resc, "setTextColor", textColor)
                setImageViewResource(R.id.ic_bolt, R.drawable.ic_bolt)
                setInt(R.id.ic_bolt, "setColorFilter", textColor)
                setInt(R.id.labor_current, "setTextColor", textColor)
                setInt(R.id.labor_max, "setTextColor", textColor)
                setInt(R.id.labor_resc, "setTextColor", textColor)
                setImageViewResource(R.id.ic_labor, R.drawable.ic_drone)
                setInt(R.id.ic_labor, "setColorFilter", textColor)
                setInt(R.id.train, "setTextColor", textColor)
                setInt(R.id.train_name, "setTextColor", textColor)
                setInt(R.id.train_resc, "setTextColor", textColor)
                setImageViewResource(R.id.ic_train, R.drawable.ic_train)
                setInt(R.id.ic_train, "setColorFilter", textColor)
            }

            //设置size
            val mainSize = WidgetSize.getTextSizeMain(prefManager.widget4Size.get())
            val subSize = WidgetSize.getTextSizeSub(prefManager.widget4Size.get())
            val iconSize = WidgetSize.getIconSize(prefManager.widget4Size.get())
            val spType = TypedValue.COMPLEX_UNIT_SP
            val dpType = TypedValue.COMPLEX_UNIT_DIP

            if (!prefManager.widget4ShowRecruit.get()) {
                views.apply {
                    setViewVisibility(R.id.recruit, View.GONE)
                    setViewVisibility(R.id.refresh, View.GONE)
                }
            } else {
                views.apply {
                    setViewVisibility(R.id.recruit, View.VISIBLE)
                    setViewVisibility(R.id.refresh, View.VISIBLE)
                    setTextViewTextSize(R.id.recruit, spType, subSize)
                    setTextViewTextSize(R.id.refresh, spType, subSize)
                }

                val recruitCache = prefManager.recruitCache.get()
                val refreshCache = prefManager.refreshCache.get()
                //recruit
                val now = getCurrentTs()
                val completeCount = when {
                    recruitCache.completeTime == -1L -> recruitCache.complete
                    now > recruitCache.completeTime -> recruitCache.complete + 1
                    else -> recruitCache.complete
                }

                views.apply {
                    setTextViewText(
                        R.id.recruit,
                        "公开招募 $completeCount/${recruitCache.max}"
                    )
                }
                //refresh
                val count = when {
                    refreshCache.completeTime == -1L -> refreshCache.count
                    now > refreshCache.completeTime -> refreshCache.count + 1
                    else -> refreshCache.count
                }
                views.apply {
                    setTextViewText(
                        R.id.refresh,
                        "公招刷新 $count/${refreshCache.max}"
                    )
                }
            }

            if (!prefManager.widget4ShowDatabase.get()) {
                views.apply {
                    setViewVisibility(R.id.ap_layout, View.GONE)
                    setViewVisibility(R.id.labor_layout, View.GONE)
                }

            } else {
                views.apply {
                    setViewVisibility(R.id.ap_layout, View.VISIBLE)
                    setViewVisibility(R.id.labor_layout, View.VISIBLE)
                    setTextViewTextSize(R.id.ap_max, spType, subSize)
                    setTextViewTextSize(R.id.ap_resc, spType, subSize)
                    setTextViewTextSize(R.id.ap_current, spType, mainSize)
                    setTextViewTextSize(R.id.labor_max, spType, subSize)
                    setTextViewTextSize(R.id.labor_resc, spType, subSize)
                    setTextViewTextSize(R.id.labor_current, spType, mainSize)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    views.apply {
                        setViewLayoutHeight(R.id.ic_bolt, iconSize.toFloat(), dpType)
                        setViewLayoutWidth(R.id.ic_bolt, iconSize.toFloat(), dpType)
                        setViewLayoutHeight(R.id.ic_labor, iconSize.toFloat(), dpType)
                        setViewLayoutWidth(R.id.ic_labor, iconSize.toFloat(), dpType)
                    }
                } else {
                    val size = dpToPx(iconSize)
//                    var icBolt = R.drawable.ic_bolt
//                    var icLabor = R.drawable.ic_drone
//                    when (prefManager.widgetTextColor.get()) {
//                        WidgetTextColor.BLACK -> {
//                            icBolt = R.drawable.ic_bolt_black
//                            icLabor = R.drawable.ic_drone_black
//                        }
//
//                        WidgetTextColor.WHITE -> {
//                            icBolt = R.drawable.ic_bolt
//                            icLabor = R.drawable.ic_drone
//                        }
//                    }

                    val bitmap1 =
                        ResourcesCompat.getDrawable(
                            context.resources,
                            getTargetDrawableId(R.drawable.ic_bolt, prefManager.widgetTextColor),
                            null
                        )?.toBitmap()!!
                    val scaledBitmap1 = Bitmap.createScaledBitmap(bitmap1, size, size, true)
                    views.setImageViewBitmap(R.id.ic_bolt, scaledBitmap1)

                    val bitmap2 =
                        ResourcesCompat.getDrawable(
                            context.resources,
                            getTargetDrawableId(R.drawable.ic_drone, prefManager.widgetTextColor),
                            null
                        )?.toBitmap()!!
                    val scaledBitmap2 = Bitmap.createScaledBitmap(bitmap2, size, size, true)
                    views.setImageViewBitmap(R.id.ic_labor, scaledBitmap2)
                }

                //apply data
                fun Long.toMinutes() = this / (60)
                val now = getCurrentTs()
                val apCache = prefManager.apCache.get()
                val apMax = apCache.max

                val current = when {
                    apCache.current >= apMax -> apCache.current
                    now > apCache.recoverTime -> apMax
                    else -> apMax - (apCache.recoverTime - now).toMinutes() / 6 - 1
                }

                views.apply {
                    setTextViewText(R.id.ap_current, "$current")
                    setTextViewText(R.id.ap_max, "/$apMax")
                    setTextViewText(
                        R.id.ap_resc,
                        TimeUtils.getRemainTimeMinStr(apCache.recoverTime - now)
                    )
                }
                //labor
                val laborCache = prefManager.laborCache.get()
                val max = laborCache.max
                val curr = when {
                    laborCache.remainSec == 0L -> laborCache.max
                    else -> {
                        val progress =
                            (now - laborCache.lastSyncTs) * (laborCache.max - laborCache.current)
                        val calculated =
                            ((progress / laborCache.remainSec) + laborCache.current).toInt()
                        calculated.coerceAtMost(laborCache.max)
                    }
                }
                views.apply {
                    setTextViewText(R.id.labor_current, "$curr")
                    setTextViewText(R.id.labor_max, "/$max")
                    setTextViewText(
                        R.id.labor_resc,
                        TimeUtils.getRemainTimeMinStr(laborCache.remainSec - now + laborCache.lastSyncTs)
                    )

                }
            }
            if (!prefManager.widget4ShowTrain.get()) {
                views.setViewVisibility(R.id.train_layout, View.GONE)
            } else {
                views.apply {
                    setViewVisibility(R.id.train_layout, View.VISIBLE)
                    setTextViewTextSize(R.id.train, spType, subSize)
                    setTextViewTextSize(R.id.train_resc, spType, subSize)
                    setTextViewTextSize(R.id.train_name, spType, mainSize)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    views.apply {
                        setViewLayoutHeight(R.id.ic_train, iconSize.toFloat(), dpType)
                        setViewLayoutWidth(R.id.ic_train, iconSize.toFloat(), dpType)
                    }
                } else {
                    val size = dpToPx(iconSize)
                    val bitmap =
                        ResourcesCompat.getDrawable(
                            context.resources,
                            getTargetDrawableId(R.drawable.ic_train, prefManager.widgetTextColor),
                            null
                        )?.toBitmap()!!
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
                    views.setImageViewBitmap(R.id.ic_train, scaledBitmap)
                }

                val now = getCurrentTs()
                val trainCache = prefManager.trainCache.get()
                if (trainCache.isnull) {
                    views.setTextViewText(R.id.train_name, "暂无数据")
                } else {
                    when (trainCache.status) {
                        -1L -> {
                            views.setTextViewText(R.id.train_name, "空闲中")
                            views.setTextViewText(R.id.train_resc, "idle")
                        }

                        0L -> {
                            views.setTextViewText(R.id.train_name, trainCache.trainee)
                            views.setTextViewText(R.id.train_resc, "completed")
                        }

                        1L -> {
                            views.setTextViewText(R.id.train_name, trainCache.trainee)
                            if (now > trainCache.completeTime) {
                                views.setTextViewText(R.id.train_resc, "completed")
                            } else {
                                views.setTextViewText(
                                    R.id.train_resc,
                                    TimeUtils.getRemainTimeMinStr(now - trainCache.completeTime)
                                )
                            }
                        }

                        else -> {
                            views.setTextViewText(R.id.train_name, "status错误")
                            Timber.e("trainCache.status ${trainCache.status}")
                        }
                    }
                }
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


            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context?) {
        val workRequest: PeriodicWorkRequest = PeriodicWorkRequest.Builder(
            SklandWorker::class.java,
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS * 2, TimeUnit.MILLISECONDS
        ).build()
        WorkManager.getInstance(context!!)
            .enqueueUniquePeriodicWork(
                WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
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
//                playFadeOutAnimation(context, appWidgetId)
            }
        }
    }

//    private fun playFadeOutAnimation(context: Context, appWidgetId: Int) {
//
//
////        val views = RemoteViews(context.packageName, R.layout.widget_attendance).apply {
////            setImageViewResource(R.id.loading, R.drawable.click_anim)
////        }
////
////        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)
////
////        // 启动动画
////        views.setImageViewResource(R.id.loading, R.drawable.click_anim)
////        AppWidgetManager.getInstance(context).partiallyUpdateAppWidget(appWidgetId, views)
//
//
////        val appWidgetManager = AppWidgetManager.getInstance(context)
////
////        // 1. 显示视图 (初始alpha=0.8)
////        val showViews = RemoteViews(context.packageName, R.layout.widget_attendance).apply {
////            setViewVisibility(R.id.loading, View.VISIBLE)
////            setFloat(R.id.loading, "setAlpha", 0.8f)
////        }
////        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, showViews)
////
////        // 2. 渐隐动画 (分5步)
////        for (i in 1..5) {
////            Handler(Looper.getMainLooper()).postDelayed({
////                val animatedViews =
////                    RemoteViews(context.packageName, R.layout.widget_attendance).apply {
////                        // 计算当前alpha (从0.8线性递减到0)
////                        val alpha = 0.8f - (0.8f * i / 5f)
////                        setFloat(R.id.loading, "setAlpha", alpha)
////
////                        // 最后一步隐藏视图
////                        if (i == 5) {
////                            setViewVisibility(R.id.loading, View.GONE)
////                        }
////                    }
////                appWidgetManager.partiallyUpdateAppWidget(appWidgetId, animatedViews)
////            }, (i * 80).toLong()) // 每80毫秒一帧，总共400毫秒动画
////        }
//    }

}