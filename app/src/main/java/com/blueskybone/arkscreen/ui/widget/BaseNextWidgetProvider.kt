package com.blueskybone.arkscreen.ui.widget

import com.blueskybone.arkscreen.platform.widget.WidgetWorkScheduler
import com.blueskybone.arkscreen.domain.usecase.widget.WidgetAccountResolver
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.blueskybone.arkscreen.domain.model.widget.WidgetConfig
import android.content.Intent
import android.widget.RemoteViews
import com.blueskybone.arkscreen.R
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
import com.blueskybone.arkscreen.ui.widget.model.WidgetVisualStyle
import com.blueskybone.arkscreen.platform.time.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.koin.java.KoinJavaComponent.getKoin
import timber.log.Timber
import android.os.SystemClock

data class NextWidgetRenderData(
    val items: List<WidgetInfoItem>,
    val compactItems: Map<WidgetInfoType, WidgetCompactItem>,
    val attendance: WidgetInfoItem?,
    val lastRefreshTs: Long,
    val cacheAccountInfo: CacheAccountInfo?,
    val visualStyle: WidgetVisualStyle,
    val config: WidgetConfig,
)

/**
 * 新版桌面组件共用的数据与交互管线。
 *
 * 子类只负责选择已配置的信息槽位，并渲染对应尺寸的 RemoteViews 层级。
 */
abstract class BaseNextWidgetProvider : AppWidgetProvider() {
    private val snapshots: com.blueskybone.arkscreen.data.local.cache.RealtimeCacheStore by getKoin().inject()
    private val configs: com.blueskybone.arkscreen.domain.repository.WidgetConfigRepository by getKoin().inject()
    private val accounts: com.blueskybone.arkscreen.domain.repository.AccountRepository by getKoin().inject()
    private val settings: SettingPrefManager by getKoin().inject()
    private val attendanceStates: AttendanceStateRepository by getKoin().inject()
    protected val templatePrefs: WidgetTemplatePrefManager by getKoin().inject()

    protected abstract val pendingIntentOffset: Int
    protected abstract fun defaultTypes(config: WidgetConfig): List<WidgetInfoType>
    protected abstract fun selectedTypeNames(config: WidgetConfig): List<String>
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
        val startedAt = SystemClock.elapsedRealtime()
        Timber.tag("Widget").i(
            "Provider render started: provider=%s widgetCount=%d",
            javaClass.simpleName,
            appWidgetIds.size,
        )
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val accountList = accounts.observeSkAcc().first()
                val current = accounts.observeCurrentSkAcc().first()
                appWidgetIds.forEach { appWidgetId ->
                    if (appWidgetManager.getAppWidgetInfo(appWidgetId) == null) return@forEach
                    val config = configs.load(appWidgetId)
                    val account = com.blueskybone.arkscreen.domain.usecase.widget.WidgetAccountResolver.resolve(config, accountList, current)
                    if (account == null) {
                        val views = RemoteViews(context.packageName, R.layout.widget_account_missing)
                        views.setOnClickPendingIntent(R.id.widget_root, WidgetAccountConfigActivity.pendingIntent(context, appWidgetId))
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                        return@forEach
                    }
                    val data = loadRenderData(context, account, config)
                    appWidgetManager.updateAppWidget(
                        appWidgetId,
                        styledViews(context, appWidgetId, data),
                    )
                }
                Timber.tag("Widget").i(
                    "Provider render completed: provider=%s widgetCount=%d durationMs=%d",
                    javaClass.simpleName,
                    appWidgetIds.size,
                    SystemClock.elapsedRealtime() - startedAt,
                )
            } catch (error: Exception) {
                Timber.tag("Widget").e(
                    error,
                    "Provider render failed: provider=%s widgetCount=%d",
                    javaClass.simpleName,
                    appWidgetIds.size,
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    final override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: android.os.Bundle) {
        com.blueskybone.arkscreen.platform.widget.WidgetUpdateDispatcher(context).render(id)
    }

    final override fun onEnabled(context: Context) {
        WidgetWorkScheduler.onWidgetEnabled(context)
        super.onEnabled(context)
    }

    final override fun onDisabled(context: Context) {
        WidgetWorkScheduler.cancelIfNoWidgets(context)
        super.onDisabled(context)
    }

    final override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try { configs.delete(appWidgetIds.toList()) } finally { pending.finish() }
        }
    }

    final override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                configs.restore(oldWidgetIds, newWidgetIds)
                com.blueskybone.arkscreen.platform.widget.WidgetUpdateDispatcher(context).renderAll()
            } finally { pending.finish() }
        }
    }

    private suspend fun loadRenderData(context: Context, account: com.blueskybone.arkscreen.domain.model.account.AccountSk, config: WidgetConfig): NextWidgetRenderData {
        val cache = snapshots.read(account)
        val cacheAccountInfo = CacheAccountInfo(account.uid, account.nickName, account.official)
        val attendanceState = cacheAccountInfo
            .uid
            .takeIf(String::isNotBlank)
            ?.let { attendanceStates.get(AccountType.SK, it) }
        val parsedTypes = selectedTypeNames(config)
            .mapNotNull { name -> runCatching { WidgetInfoType.valueOf(name) }.getOrNull() }
            .filterNot { it == WidgetInfoType.ATTENDANCE }
            .distinct()
        val defaults = defaultTypes(config)
        val selectedTypes = parsedTypes.takeIf { it.size == defaults.size } ?: defaults
        val appClock: AppClock = getKoin().get()
        val items = WidgetInfoMapper(context, cache, settings, appClock)
            .mapAll(attendanceState = attendanceState)
            .associateBy(WidgetInfoItem::type)
        val compactMapper = WidgetCompactInfoMapper(context, cache, appClock)
        return NextWidgetRenderData(
            items = selectedTypes.map(items::getValue),
            compactItems = selectedTypes.associateWith { type ->
                compactMapper.map(items.getValue(type))
            },
            attendance = items[WidgetInfoType.ATTENDANCE]
                .takeIf { settings.backAutoAtd.get() },
            lastRefreshTs = cache.apCache.lastSyncTs,
            cacheAccountInfo = cacheAccountInfo,
            visualStyle = WidgetVisualStyle.fromKey(config.theme.orEmpty()),
            config = config,
        )
    }

    private fun styledViews(context: Context, id: Int, data: NextWidgetRenderData): RemoteViews {
        val remote = createRemoteViews(context, id, data)
        remote.setInt(R.id.widget_background, "setImageAlpha", ((100 - data.config.transparency.coerceIn(0, 100)) * 255 / 100))
        // Preserve each layout's typography hierarchy when scaling.
        val xml = context.resources.getLayout(remote.layoutId)
        try {
            var event = xml.eventType
            while (event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                if (event == org.xmlpull.v1.XmlPullParser.START_TAG && xml.name == "TextView") {
                    val attrs = context.obtainStyledAttributes(
                        android.util.Xml.asAttributeSet(xml),
                        R.styleable.WidgetScaledText,
                    )
                    val viewId = attrs.getResourceId(R.styleable.WidgetScaledText_android_id, 0)
                    val px = attrs.getDimension(R.styleable.WidgetScaledText_android_textSize, 0f)
                    attrs.recycle()
                    if (viewId != 0 && px > 0) remote.setTextViewTextSize(viewId, android.util.TypedValue.COMPLEX_UNIT_PX,
                        px * data.config.fontScale.coerceIn(0.85f, 1.15f))
                }
                event = xml.next()
            }
        } finally { xml.close() }
        return remote
    }

    protected fun palette(data: NextWidgetRenderData): WidgetPalette =
        data.visualStyle.palette

    protected fun backgroundRes(
        data: NextWidgetRenderData,
    ): Int = data.visualStyle.palette.backgroundRes

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
