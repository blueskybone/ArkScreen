package com.blueskybone.arkscreen.task.attendance

import android.content.Context
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.sklandAttendance
import com.blueskybone.arkscreen.room.ArkDatabase
import com.blueskybone.arkscreen.util.updateNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


suspend fun doSklandAttendance(context: Context) {
    val database = ArkDatabase.getDatabase(APP)
    val accountSkDao = database.getAccountSkDao()
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
        val msg = sklandAttendance(account)
        Timber.i(account.nickName + " : " + msg)
        updateNotification(
            context,
            "正在签到中 (${idx + 1}/${accountList.size})",
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
        "签到完成 (${accountList.size}/${accountList.size})",
        "",
        channelId,
        channelName
    )
}