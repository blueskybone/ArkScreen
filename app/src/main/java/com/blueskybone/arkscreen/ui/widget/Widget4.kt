package com.blueskybone.arkscreen.ui.widget

import android.annotation.SuppressLint
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
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.CachePrefManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetTextColor
import com.blueskybone.arkscreen.ui.widget.WidgetReceiver.Companion.MANUAL_UPDATE
import com.blueskybone.arkscreen.platform.time.TimeUtils
import com.blueskybone.arkscreen.platform.time.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.dpToPx
import com.hjq.toast.Toaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */
class Widget4 : AppWidgetProvider() {
    private val prefManager: SettingPrefManager by KoinJavaComponent.getKoin().inject()
    private val cachePrefManager: CachePrefManager by KoinJavaComponent.getKoin().inject()
    private val accountRepository: AccountRepository by KoinJavaComponent.getKoin().inject()
    private val appClock: AppClock by KoinJavaComponent.getKoin().inject()

    companion object {
        const val START_GAME = "com.blueskybone.arkscreen.START_GAME"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val formatter = WidgetContentFormatter(cachePrefManager, appClock)
        appWidgetIds.forEach { appWidgetId ->
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
                setInt(R.id.meeting, "setTextColor", textColor)

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
                setInt(R.id.last_update_label, "setTextColor", textColor)
                setInt(R.id.last_update_time, "setTextColor", textColor)
            }

            //设置size
            val mainSize = WidgetSize.getTextSizeMain(prefManager.widget4Size.get())
            val subSize = WidgetSize.getTextSizeSub(prefManager.widget4Size.get())
            val iconSize = WidgetSize.getIconSize(prefManager.widget4Size.get())
            val spType = TypedValue.COMPLEX_UNIT_SP
            val dpType = TypedValue.COMPLEX_UNIT_DIP
            views.setTextViewTextSize(R.id.last_update_label, spType, subSize)
            views.setTextViewTextSize(R.id.last_update_time, spType, subSize)

            if (!prefManager.widget4ShowRecruit.get()) {
                views.apply {
                    setViewVisibility(R.id.recruit, View.GONE)
                    setViewVisibility(R.id.refresh, View.GONE)
                    setViewVisibility(R.id.meeting, View.GONE)
                }
            } else {
                views.apply {
                    setViewVisibility(R.id.recruit, View.VISIBLE)
                    setViewVisibility(R.id.refresh, View.VISIBLE)
                    setViewVisibility(R.id.meeting, View.VISIBLE)
                    setTextViewTextSize(R.id.recruit, spType, subSize)
                    setTextViewTextSize(R.id.refresh, spType, subSize)
                    setTextViewTextSize(R.id.meeting, spType, subSize)
                }

                val recruitCache = cachePrefManager.recruitCache.get()
                val refreshCache = cachePrefManager.refreshCache.get()
                //recruit
                val now = getCurrentTs(appClock)
                val completeCount = when {
                    recruitCache.completeTime == -1L -> recruitCache.complete
                    now > recruitCache.completeTime -> recruitCache.complete + 1
                    else -> recruitCache.complete
                }

                views.apply {
                    setTextViewText(
                        R.id.recruit,
                        "招募 $completeCount/${recruitCache.max}"
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
                        "刷新 $count/${refreshCache.max}"
                    )
                }
                val meetDisplay = formatter.format("meet")
                val meet = meetDisplay.secondary.ifBlank { meetDisplay.primary }
                views.apply {
                    setTextViewText(
                        R.id.meeting,
                        "线索 $meet"
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
                    val bitmap1 =
                        ResourcesCompat.getDrawable(
                            context.resources,
                            getTargetDrawableId(R.drawable.ic_bolt, prefManager.widgetTextColor.get()),
                            null
                        )?.toBitmap()!!
                    val scaledBitmap1 = Bitmap.createScaledBitmap(bitmap1, size, size, true)
                    views.setImageViewBitmap(R.id.ic_bolt, scaledBitmap1)

                    val bitmap2 =
                        ResourcesCompat.getDrawable(
                            context.resources,
                            getTargetDrawableId(R.drawable.ic_drone, prefManager.widgetTextColor.get()),
                            null
                        )?.toBitmap()!!
                    val scaledBitmap2 = Bitmap.createScaledBitmap(bitmap2, size, size, true)
                    views.setImageViewBitmap(R.id.ic_labor, scaledBitmap2)
                }

                val ap = formatter.format("ap", compact = true)
                views.apply {
                    setTextViewText(R.id.ap_current, ap.primary)
                    setTextViewText(R.id.ap_max, ap.secondary.takeIf(String::isNotBlank)?.let { "/$it" } ?: "")
                    setTextViewText(R.id.ap_resc, formatter.format("ap").secondary)
                }
                val labor = formatter.format("labor", compact = true)
                views.apply {
                    setTextViewText(R.id.labor_current, labor.primary)
                    setTextViewText(
                        R.id.labor_max,
                        labor.secondary.takeIf(String::isNotBlank)?.let { "/$it" } ?: "",
                    )
                    setTextViewText(R.id.labor_resc, formatter.format("labor").secondary)
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
                            getTargetDrawableId(R.drawable.ic_train, prefManager.widgetTextColor.get()),
                            null
                        )?.toBitmap()!!
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
                    views.setImageViewBitmap(R.id.ic_train, scaledBitmap)
                }

                val train = formatter.format("train")
                views.setTextViewText(R.id.train_name, train.primary)
                views.setTextViewText(R.id.train_resc, train.secondary)
            }

            val lastSyncTs = listOf(
                cachePrefManager.apCache.get().lastSyncTs,
                cachePrefManager.laborCache.get().lastSyncTs,
                cachePrefManager.trainCache.get().lastSyncTs,
                cachePrefManager.recruitCache.get().lastSyncTs,
                cachePrefManager.refreshCache.get().lastSyncTs,
                cachePrefManager.meetCache.get().lastSyncTs,
            ).maxOrNull() ?: 0L
            views.setTextViewText(
                R.id.last_update_time,
                if (lastSyncTs > 0L) {
                    TimeUtils.getTimeStr(lastSyncTs * 1000, "MM-dd HH:mm")
                } else {
                    context.getString(R.string.widget_last_update_time_empty)
                },
            )

            val updateIntent = Intent(context, WidgetReceiver::class.java).apply {
                action = MANUAL_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntentUpdate = PendingIntent.getBroadcast(
                context,
                appWidgetId, // 使用 widgetId 作为 requestCode 确保唯一性
                updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.grid_layout, pendingIntentUpdate)


            if (!prefManager.widget4ShowStarter.get()) {
                views.setViewVisibility(R.id.starter, View.GONE)
            } else {
                views.setViewVisibility(R.id.starter, View.VISIBLE)
                val startIntent = Intent(context, Widget4::class.java).apply {
                    action = START_GAME
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                val pendingIntentStart = PendingIntent.getBroadcast(
                    context,
                    appWidgetId, // 使用 widgetId 作为 requestCode 确保唯一性
                    startIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.starter, pendingIntentStart)
            }

            val pendingIntentRefresh = PendingIntent.getBroadcast(
                context,
                appWidgetId, // 使用 widgetId 作为 requestCode 确保唯一性
                updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.refresh_data, pendingIntentRefresh)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context?) {
        context?.let(WidgetWorkScheduler::onWidgetEnabled)
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context?) {
        context?.let(WidgetWorkScheduler::cancelIfNoWidgets)
        super.onDisabled(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == START_GAME) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                val pendingResult = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val account = accountRepository.observeCurrentSkAcc().first()
                        val packageName = if (account?.official != false) {
                            "com.hypergryph.arknights"
                        } else {
                            "com.hypergryph.arknights.bilibili"
                        }
                        openAnotherApp(context, packageName)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openAnotherApp(context: Context, packageName: String) {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Toaster.show(context.getString(R.string.game_not_found))
        }
    }

}
