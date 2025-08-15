package com.blueskybone.arkscreen.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.blueskybone.arkscreen.widget.SklandWorker
import com.hjq.toast.Toaster
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2025/2/6
 */
class WidgetReceiver : BroadcastReceiver() {

    companion object {
        const val MANUAL_UPDATE = "com.blueskybone.arkscreen.MANUAL_UPDATE"
        const val WORKER_NAME = "SklandWorker"
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == MANUAL_UPDATE) {
            intent.getStringExtra("msg")?.let { msg ->
                Toaster.show(msg)
            }
            Toaster.show("更新中...")
            Timber.i("WidgetReceiver onReceive")
            WorkManager.getInstance(context!!)
                .enqueue(OneTimeWorkRequest.from(SklandWorker::class.java))
        }
    }
}