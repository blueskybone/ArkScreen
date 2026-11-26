package com.blueskybone.arkscreen.platform.widget

import com.blueskybone.arkscreen.domain.model.settings.WidgetUpdateFrequencySetting
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.platform.widget.WidgetRefreshWorker
import com.blueskybone.arkscreen.platform.widget.WidgetUpdateDispatcher
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import org.koin.java.KoinJavaComponent.getKoin
import java.util.concurrent.TimeUnit
import timber.log.Timber

object WidgetWorkScheduler {
    const val WORKER_NAME = "WidgetRefreshWorker"
    const val ONE_TIME_WORKER_NAME = "WidgetRefreshWorkerOneTime"

    fun enqueueUpdate(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            ONE_TIME_WORKER_NAME, ExistingWorkPolicy.KEEP, request,
        )
    }

    fun onWidgetEnabled(context: Context) {
        schedule(context)
        enqueueUpdate(context)
    }

    fun ensureScheduled(context: Context) {
        if (!WidgetUpdateDispatcher(context).hasWidgets()) {
            cancelAll(context)
            return
        }
        schedule(context)
    }

    private fun schedule(context: Context) {
        val settings: SettingPrefManager by getKoin().inject()
        val intervalSeconds = WidgetUpdateFrequencySetting.getValue(settings.widgetUpdateFreq.get()).toLong()
        Timber.tag("Widget").i("Schedule refresh worker: intervalSeconds=%d", intervalSeconds)
        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
            intervalSeconds,
            TimeUnit.SECONDS,
        ).build()
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            WORKER_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancelIfNoWidgets(context: Context) {
        if (!WidgetUpdateDispatcher(context).hasWidgets()) cancelAll(context)
    }

    private fun cancelAll(context: Context) {
        Timber.tag("Widget").i("Cancel refresh workers: no active widgets")
        WorkManager.getInstance(context.applicationContext).apply {
            cancelUniqueWork(WORKER_NAME)
            cancelUniqueWork(ONE_TIME_WORKER_NAME)
        }
    }
}
