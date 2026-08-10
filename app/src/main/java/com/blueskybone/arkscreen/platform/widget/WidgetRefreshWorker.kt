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

class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val settings: SettingPrefManager by getKoin().inject()
    private val innerPrefs: InnerPrefManager by getKoin().inject()
    private val accountRepository: AccountRepository by getKoin().inject()
    private val getRealTime: GetRealTimeUseCase by getKoin().inject()
    private val widgetUpdates: WidgetUpdateDispatcher by getKoin().inject()
    private val appClock: AppClock by getKoin().inject()

    override suspend fun doWork(): Result {
        if (!widgetUpdates.hasWidgets()) return Result.success()
        try {
            if (!settings.powerSavingMode.get()) {
                accountRepository.observeCurrentSkAcc().first()?.let { account ->
                    getRealTime(account).getOrThrow()
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // Rendering cached values must not depend on network availability.
            Timber.w(error, "Widget 实时数据同步失败，继续使用本地缓存")
        } finally {
            innerPrefs.lastWidgetRefreshTs.set(TimeUtils.getCurrentTs(appClock))
            widgetUpdates.renderAll()
        }
        return Result.success()
    }
}
