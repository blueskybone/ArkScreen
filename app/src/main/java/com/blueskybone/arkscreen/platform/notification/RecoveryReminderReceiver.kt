package com.blueskybone.arkscreen.platform.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.java.KoinJavaComponent.getKoin

class RecoveryReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val controller = getKoin().get<RecoveryReminderController>()
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> controller.restoreAfterBoot()
            RecoveryReminderController.ACTION_FIRE -> controller.onAlarm(
                intent.getStringExtra(RecoveryReminderController.EXTRA_KEY) ?: return,
                intent.getStringExtra(RecoveryReminderController.EXTRA_UID) ?: return,
                intent.getLongExtra(RecoveryReminderController.EXTRA_AT, -1L),
            )
        }
    }
}
