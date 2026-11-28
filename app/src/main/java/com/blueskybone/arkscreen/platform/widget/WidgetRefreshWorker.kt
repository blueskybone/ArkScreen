package com.blueskybone.arkscreen.platform.widget

import com.blueskybone.arkscreen.domain.usecase.widget.WidgetAccountResolver
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import org.koin.java.KoinJavaComponent.getKoin
import timber.log.Timber
import com.blueskybone.arkscreen.platform.time.TimeUtils
import android.os.SystemClock

class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val settings: SettingPrefManager by getKoin().inject()
    private val innerPrefs: InnerPrefManager by getKoin().inject()
    private val accountRepository: AccountRepository by getKoin().inject()
    private val getRealTime: GetRealTimeUseCase by getKoin().inject()
    private val widgetUpdates: WidgetUpdateDispatcher by getKoin().inject()
    private val appClock: AppClock by getKoin().inject()

    private val configs: com.blueskybone.arkscreen.domain.repository.WidgetConfigRepository by getKoin().inject()

    override suspend fun doWork(): Result {
        val startedAt = SystemClock.elapsedRealtime()
        if (!widgetUpdates.hasWidgets()) {
            Timber.tag("Widget").i("Refresh skipped: no active widgets")
            return Result.success()
        }
        val powerSaving = settings.powerSavingMode.get()
        Timber.tag("Widget").i("Refresh started: powerSaving=%s", powerSaving)
        var failed = false
        var retryable = false
        var networkSynced = false
        var dataSource = if (powerSaving) "cache" else "network"
        try {
            if (!powerSaving) {
                val (all, current) = readWidgetAccounts {
                    accountRepository.observeSkAcc().first() to accountRepository.observeCurrentSkAcc().first()
                }
                val selected = widgetUpdates.widgetIds().mapNotNull { id ->
                    val config = configs.load(id)
                    WidgetAccountResolver.resolve(config, all, current)
                }.distinctBy { it.channelMasterId to it.uid }
                for (account in selected) {
                    try {
                        getRealTime(account).getOrThrow()
                        networkSynced = true
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        failed = true
                        retryable = retryable || isRetryableWidgetError(error)
                        dataSource = "cache_after_failure"
                        Timber.tag("Widget").w(error, "Account refresh failed; retaining its own snapshot")
                    }
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // 缓存内容的渲染不能依赖当前网络是否可用。
            failed = true
            retryable = retryable || isRetryableWidgetError(error)
            dataSource = "cache_after_failure"
            Timber.tag("Widget").w(error, "实时数据同步失败，继续使用本地缓存")
        }
        // Cancellation leaves both the timestamp and desktop unchanged.
        kotlinx.coroutines.currentCoroutineContext().ensureActive()
        if (networkSynced && !failed) {
            innerPrefs.lastWidgetRefreshTs.set(TimeUtils.getCurrentTs(appClock))
        }
        widgetUpdates.renderAll()
        Timber.tag("Widget").i(
            "Refresh completed: source=%s durationMs=%d",
            dataSource,
            SystemClock.elapsedRealtime() - startedAt,
        )
        return when {
            retryable && runAttemptCount < 3 -> Result.retry()
            failed -> Result.failure()
            else -> Result.success()
        }
    }
}
