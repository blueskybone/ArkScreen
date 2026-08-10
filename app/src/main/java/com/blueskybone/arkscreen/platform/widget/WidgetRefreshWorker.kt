package com.blueskybone.arkscreen.platform.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import com.blueskybone.arkscreen.platform.schedule.AttendanceWorkScheduler
import com.blueskybone.arkscreen.ui.widget.Widget1
import com.blueskybone.arkscreen.ui.widget.Widget2
import com.blueskybone.arkscreen.ui.widget.Widget3
import com.blueskybone.arkscreen.ui.widget.Widget4
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import org.koin.java.KoinJavaComponent.getKoin
import timber.log.Timber

class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val settings: SettingPrefManager by getKoin().inject()
    private val accountRepository: AccountRepository by getKoin().inject()
    private val getRealTime: GetRealTimeUseCase by getKoin().inject()

    override suspend fun doWork(): Result {
        return try {
            if (settings.autoAttendance.get()) {
                AttendanceWorkScheduler.enqueue(applicationContext, force = false)
            }
            if (!settings.powerSavingMode.get()) {
                accountRepository.observeCurrentSkAcc().first()?.let { account ->
                    getRealTime(account).getOrThrow()
                }
            }
            broadcastUpdates()
            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.e(error, "Widget 刷新失败")
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    private fun broadcastUpdates() {
        val manager = AppWidgetManager.getInstance(applicationContext)
        PROVIDERS.forEach { provider ->
            val ids = manager.getAppWidgetIds(ComponentName(applicationContext, provider))
            if (ids.isEmpty()) return@forEach
            applicationContext.sendBroadcast(
                Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .setComponent(ComponentName(applicationContext, provider))
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            )
        }
    }

    private companion object {
        const val MAX_RETRIES = 3
        val PROVIDERS = listOf(
            Widget1::class.java,
            Widget2::class.java,
            Widget3::class.java,
            Widget4::class.java,
        )
    }
}
