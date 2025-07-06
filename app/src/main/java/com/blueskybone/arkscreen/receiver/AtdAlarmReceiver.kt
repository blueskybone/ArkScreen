package com.blueskybone.arkscreen.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.network.NetWorkTask
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.ArkDatabase
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.updateNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2025/2/4
 */

class AtdAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        println("Received intent: $intent")
        if (intent.action == "android.intent.action.BOOT_COMPLETED") { //Intent.ACTION_BOOT_COMPLETED
            val prefManager: PrefManager by getKoin().inject()
            if (!prefManager.backAutoAtd.get()) return
            APP.setDailyAlarm()
            return
        } else {
            val prefManager: PrefManager by getKoin().inject()
            val database = ArkDatabase.getDatabase(APP)
            val accountSkDao = database.getAccountSkDao()
            CoroutineScope(Dispatchers.IO).launch {
                val accountList = accountSkDao.getAll()
                val channelId = "atd_notify_channel"
                val channelName = "签到通知"
                for ((idx, account) in accountList.withIndex()) {
                    updateNotification(
                        context,
                        "正在签到中 (${idx + 1}/${accountList.size})",
                        account.nickName,
                        channelId,
                        channelName
                    )
                    val msg = NetWorkTask.sklandAttendance(account)
                    Timber.i(account.nickName + " : " + msg)
                    updateNotification(
                        context,
                        "正在签到中 (${idx + 1}/${accountList.size})",
                        account.nickName + " : " + msg,
                        channelId,
                        channelName
                    )
                    Thread.sleep(500)
                }
                updateNotification(
                    context,
                    "签到完成 (${accountList.size}/${accountList.size})",
                    "",
                    channelId,
                    channelName
                )
            }
            prefManager.lastAttendanceTs.set(getCurrentTs())
        }
    }
}

