package com.blueskybone.arkscreen.ui.widget

import com.blueskybone.arkscreen.platform.widget.WidgetWorkScheduler
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.platform.widget.WidgetRefreshWorker
import com.blueskybone.arkscreen.platform.widget.WidgetUpdateDispatcher
import com.hjq.toast.Toaster
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2025/2/6
 */
class WidgetReceiver : BroadcastReceiver() {

    companion object {
        const val MANUAL_UPDATE = "com.blueskybone.arkscreen.MANUAL_UPDATE"
        const val WORKER_NAME = WidgetWorkScheduler.WORKER_NAME
        const val ONE_TIME_WORKER_NAME = WidgetWorkScheduler.ONE_TIME_WORKER_NAME

        fun enqueueUpdate(context: Context) = WidgetWorkScheduler.enqueueUpdate(context)

        fun renderCached(context: Context) {
            WidgetUpdateDispatcher(context).renderAll()
        }
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == MANUAL_UPDATE) {
            val receiverContext = context ?: return
            intent.getStringExtra("msg")?.let { msg ->
                Toaster.show(msg)
            }
            Toaster.show(receiverContext.getString(R.string.widget_updating))
            Timber.tag("Widget").i("Manual refresh requested")
            enqueueUpdate(receiverContext)
        }
    }
}
