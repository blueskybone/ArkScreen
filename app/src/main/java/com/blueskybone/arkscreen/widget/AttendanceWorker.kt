package com.blueskybone.arkscreen.widget

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blueskybone.arkscreen.base.PrefManager
import com.blueskybone.arkscreen.base.data.AccountSk
import com.blueskybone.arkscreen.network.NetWorkTask.sklandAttendance
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.TimeUtils.getDayNum
import org.koin.java.KoinJavaComponent.getKoin

/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */
class AttendanceWorker(context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {
        try {
            val prefManager: PrefManager by getKoin().inject()
            if (!prefManager.isAutoCheckOn.get()) return Result.success()
            val lastAttendanceTs = prefManager.LastAttendanceTs.get()
            val currentTs = getCurrentTs()
            if (getDayNum(currentTs) <= getDayNum(lastAttendanceTs)) return Result.success()
            val accountList = prefManager.ListAccountSk.get()
            for (account in accountList) {
                sklandAttendance(account.token, account.uid, account.channelMasterId)
            }
            prefManager.LastAttendanceTs.set(currentTs)
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}