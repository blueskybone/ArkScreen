package com.blueskybone.arkscreen.ui.widget

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.platform.widget.WidgetRefreshWorker
import com.blueskybone.arkscreen.platform.widget.WidgetUpdateDispatcher
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetUpdateFreq
import com.blueskybone.arkscreen.ui.widget.WidgetReceiver.Companion.WORKER_NAME
import org.koin.java.KoinJavaComponent.getKoin
import java.util.concurrent.TimeUnit

object WidgetWorkScheduler {
    fun onWidgetEnabled(context: Context) {
        schedule(context)
        WidgetReceiver.enqueueUpdate(context)
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
        val intervalSeconds = WidgetUpdateFreq.getValue(settings.widgetUpdateFreq.get()).toLong()
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
        WorkManager.getInstance(context.applicationContext).apply {
            cancelUniqueWork(WORKER_NAME)
            cancelUniqueWork(WidgetReceiver.ONE_TIME_WORKER_NAME)
        }
    }
}
