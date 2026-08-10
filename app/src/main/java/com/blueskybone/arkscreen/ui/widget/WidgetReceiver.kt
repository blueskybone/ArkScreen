package com.blueskybone.arkscreen.ui.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.blueskybone.arkscreen.platform.widget.WidgetRefreshWorker
import com.hjq.toast.Toaster
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2025/2/6
 */
class WidgetReceiver : BroadcastReceiver() {

    companion object {
        const val MANUAL_UPDATE = "com.blueskybone.arkscreen.MANUAL_UPDATE"
        const val WORKER_NAME = "WidgetRefreshWorker"
        const val ONE_TIME_WORKER_NAME = "WidgetRefreshWorkerOneTime"

        fun enqueueUpdate(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORKER_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == MANUAL_UPDATE) {
            intent.getStringExtra("msg")?.let { msg ->
                Toaster.show(msg)
            }
            Toaster.show("更新中...")
            Timber.i("WidgetReceiver onReceive")
            enqueueUpdate(context!!)
        }
    }
}
