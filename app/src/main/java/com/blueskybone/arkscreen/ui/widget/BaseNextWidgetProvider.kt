package com.blueskybone.arkscreen.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.CachePrefManager
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.data.local.pref.WidgetTemplatePrefManager
import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.model.cache.CacheAccountInfo
import com.blueskybone.arkscreen.domain.repository.AttendanceStateRepository
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoItem
import com.blueskybone.arkscreen.ui.widget.model.WidgetCompactInfoMapper
import com.blueskybone.arkscreen.ui.widget.model.WidgetCompactItem
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoMapper
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoState
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoType
import com.blueskybone.arkscreen.ui.widget.model.WidgetPalette
import com.blueskybone.arkscreen.ui.widget.model.WidgetBackgroundSize
import com.blueskybone.arkscreen.ui.widget.model.WidgetVisualStyle
import com.blueskybone.arkscreen.platform.time.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import timber.log.Timber

data class NextWidgetRenderData(
    val items: List<WidgetInfoItem>,
    val compactItems: Map<WidgetInfoType, WidgetCompactItem>,
    val attendance: WidgetInfoItem?,
    val lastRefreshTs: Long,
    val cacheAccountInfo: CacheAccountInfo?,
    val visualStyle: WidgetVisualStyle,
)

/**
 * Shared data and interaction pipeline for the experimental widget providers.
 *
 * Subclasses only select their configured slot and render their size-specific
 * RemoteViews hierarchy.
 */
abstract class BaseNextWidgetProvider : AppWidgetProvider() {
    private val cache: CachePrefManager by getKoin().inject()
    private val settings: SettingPrefManager by getKoin().inject()
    private val innerPrefs: InnerPrefManager by getKoin().inject()
    private val attendanceStates: AttendanceStateRepository by getKoin().inject()
    protected val templatePrefs: WidgetTemplatePrefManager by getKoin().inject()

    protected abstract val pendingIntentOffset: Int
    protected abstract val defaultTypes: List<WidgetInfoType>
    protected abstract fun selectedTypeNames(): List<String>
    protected abstract fun createRemoteViews(
        context: Context,
        appWidgetId: Int,
        data: NextWidgetRenderData,
    ): RemoteViews

    final override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        if (appWidgetIds.isEmpty()) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val data = loadRenderData(context)
                appWidgetIds.forEach { appWidgetId ->
                    appWidgetManager.updateAppWidget(
                        appWidgetId,
                        createRemoteViews(context, appWidgetId, data),
                    )
                }
            } catch (error: Exception) {
                Timber.e(error, "Failed to render next-generation widget")
            } finally {
                pendingResult.finish()
            }
        }
    }

    final override fun onEnabled(context: Context) {
        WidgetWorkScheduler.onWidgetEnabled(context)
        super.onEnabled(context)
    }

    final override fun onDisabled(context: Context) {
        WidgetWorkScheduler.cancelIfNoWidgets(context)
        super.onDisabled(context)
    }

    private suspend fun loadRenderData(context: Context): NextWidgetRenderData {
        val cacheAccountInfo = cache.accountInfo.get().takeIf { it.uid.isNotBlank() }
        val attendanceState = cacheAccountInfo
            ?.uid
            ?.takeIf(String::isNotBlank)
            ?.let { attendanceStates.get(AccountType.SK, it) }
        val parsedTypes = selectedTypeNames()
            .mapNotNull { name -> runCatching { WidgetInfoType.valueOf(name) }.getOrNull() }
            .filterNot { it == WidgetInfoType.ATTENDANCE }
            .distinct()
        val selectedTypes = parsedTypes.takeIf { it.size == defaultTypes.size } ?: defaultTypes
        val appClock: AppClock = getKoin().get()
        val items = WidgetInfoMapper(context, cache, settings, appClock)
            .mapAll(attendanceState = attendanceState)
            .associateBy(WidgetInfoItem::type)
        val compactMapper = WidgetCompactInfoMapper(cache, appClock)
        return NextWidgetRenderData(
            items = selectedTypes.map(items::getValue),
            compactItems = selectedTypes.associateWith { type ->
                compactMapper.map(items.getValue(type))
            },
            attendance = items[WidgetInfoType.ATTENDANCE]
                .takeIf { settings.backAutoAtd.get() },
            lastRefreshTs = innerPrefs.lastWidgetRefreshTs.get(),
            cacheAccountInfo = cacheAccountInfo,
            visualStyle = WidgetVisualStyle.fromKey(templatePrefs.style.get()),
        )
    }

    protected fun palette(data: NextWidgetRenderData): WidgetPalette =
        data.visualStyle.palette

    protected fun backgroundRes(
        data: NextWidgetRenderData,
        size: WidgetBackgroundSize,
    ): Int = data.visualStyle.backgroundRes(size)

    protected fun primaryText(context: Context, item: WidgetInfoItem): String =
        item.value?.takeIf(String::isNotBlank)
            ?: item.restTime?.takeIf(String::isNotBlank)
            ?: context.getString(R.string.widget_no_data)

    protected fun refreshStatusText(context: Context, data: NextWidgetRenderData): String {
        val time = if (data.lastRefreshTs > 0L) {
            TimeUtils.getTimeStr(data.lastRefreshTs * 1000, "HH:mm")
        } else {
            context.getString(R.string.widget_last_update_time_empty)
        }
        return context.getString(R.string.widget_footer_refresh, time)
    }

    protected fun stateColor(data: NextWidgetRenderData, state: WidgetInfoState): Int =
        palette(data).stateColor(state)

    protected fun refreshIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, WidgetReceiver::class.java).apply {
            action = WidgetReceiver.MANUAL_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            pendingIntentOffset + appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
