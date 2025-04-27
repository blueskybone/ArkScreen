package com.blueskybone.arkscreen.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.sklandAttendance
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.ArkDatabase
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.TimeUtils.getDayNum
import org.koin.java.KoinJavaComponent.getKoin

/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */
class AttendanceWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(
    context,
    workerParams
) {
    override suspend fun doWork(): Result {
        try {
            val prefManager: PrefManager by getKoin().inject()
            if (!prefManager.autoAttendance.get()) return Result.success()
            val lastAttendanceTs = prefManager.lastAttendanceTs.get()
            val currentTs = getCurrentTs()
            if (getDayNum(currentTs) <= getDayNum(lastAttendanceTs)) return Result.success()

            val database = ArkDatabase.getDatabase(APP)
            val accountSkDao = database.getAccountSkDao()
            val accountList = accountSkDao.getAll()
            for (account in accountList) {
                sklandAttendance(account)
            }
            prefManager.lastAttendanceTs.set(currentTs)
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}