package com.blueskybone.arkscreen.platform.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.data.local.pref.PrefManager
import com.blueskybone.arkscreen.data.local.room.ArkDatabase
import com.blueskybone.arkscreen.util.TimeUtils
import com.blueskybone.arkscreen.util.updateNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2025/2/4
 */

class AtdAlarmReceiver : BroadcastReceiver() {
    /*
    * 后台签到的Receiver，收到定时消息后在此处执行预设定的功能。
    * */
    override fun onReceive(context: Context, intent: Intent) {
        Timber.Forest.i("Received intent: $intent")
        if (intent.action == "android.intent.action.BOOT_COMPLETED") { //Intent.ACTION_BOOT_COMPLETED
            val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()
            if (!prefManager.backAutoAtd.get()) return
            APP.setDailyAlarm()
            return
        } else {
            //TODO:签到的逻辑单独提出来。
            val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()
            val database = ArkDatabase.Companion.getDatabase(APP)
            val accountSkDao = database.getAccountSkDao()
            CoroutineScope(Dispatchers.IO).launch {
                val accountList = accountSkDao.getAll()
                val channelId = "atd_notify_channel"
                val channelName = "签到通知"

                val accountEfList = database.getAccountEfDao().getAll()
                val size = accountEfList.size + accountList.size
                for ((idx, account) in accountList.withIndex()) {
                    updateNotification(
                        context,
                        "正在签到中 (${idx + 1}/${accountList.size})",
                        account.nickName,
                        channelId,
                        channelName
                    )
//                    val msg = NetWorkTask.sklandAttendance(account)
                    val msg = "suce"
                    Timber.Forest.i(account.nickName + " : " + msg)
                    updateNotification(
                        context,
                        "正在签到中 (${idx + 1}/${accountList.size})",
                        account.nickName + " : " + msg,
                        channelId,
                        channelName
                    )
                    Thread.sleep(500)
                }


                for ((idx, account) in accountEfList.withIndex()) {
                    updateNotification(
                        context,
                        "正在签到中 (${idx + 1}/${accountEfList.size})",
                        account.nickName,
                        channelId,
                        channelName
                    )
//                    val msg = endfieldAttendance(account)
                    val msg = "succe"
                    Timber.Forest.i(account.nickName + " : " + msg)
                    updateNotification(
                        context,
                        "正在签到中 (${idx + 1}/${accountEfList.size})",
                        account.nickName + " : " + msg,
                        channelId,
                        channelName
                    )
                    withContext(Dispatchers.IO) {
                        Thread.sleep(500)
                    }
                }


                updateNotification(
                    context,
                    "签到完成 (${size}/${size})",
                    "",
                    channelId,
                    channelName
                )
            }
            //记录签到时间，避免同一天重复签到
            prefManager.lastAttendanceTs.set(TimeUtils.getCurrentTs())
        }
    }
}