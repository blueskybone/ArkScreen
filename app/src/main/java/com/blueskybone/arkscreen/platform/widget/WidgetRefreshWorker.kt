package com.blueskybone.arkscreen.platform.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import kotlinx.coroutines.CancellationException
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

    override suspend fun doWork(): Result {
        val startedAt = SystemClock.elapsedRealtime()
        if (!widgetUpdates.hasWidgets()) {
            Timber.tag("Widget").i("Refresh skipped: no active widgets")
            return Result.success()
        }
        val powerSaving = settings.powerSavingMode.get()
        Timber.tag("Widget").i("Refresh started: powerSaving=%s", powerSaving)
        var dataSource = if (powerSaving) "cache" else "network"
        try {
            if (!powerSaving) {
                accountRepository.observeCurrentSkAcc().first()?.let { account ->
                    getRealTime(account).getOrThrow()
                } ?: run { dataSource = "cache_no_account" }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // 缓存内容的渲染不能依赖当前网络是否可用。
            dataSource = "cache_after_failure"
            Timber.tag("Widget").w(error, "实时数据同步失败，继续使用本地缓存")
        } finally {
            innerPrefs.lastWidgetRefreshTs.set(TimeUtils.getCurrentTs(appClock))
            widgetUpdates.renderAll()
        }
        Timber.tag("Widget").i(
            "Refresh completed: source=%s durationMs=%d",
            dataSource,
            SystemClock.elapsedRealtime() - startedAt,
        )
        return Result.success()
    }
}
