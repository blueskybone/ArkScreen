package com.blueskybone.arkscreen.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.network.NetWorkTask
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.getGameInfoConnectionTaskTest
import com.blueskybone.arkscreen.playerinfo.ApCache
import com.blueskybone.arkscreen.playerinfo.LaborCache
import com.blueskybone.arkscreen.playerinfo.RealTimeData
import com.blueskybone.arkscreen.playerinfo.geneRealTimeData
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.ArkDatabase
import com.blueskybone.arkscreen.util.TimeUtils
import org.koin.java.KoinJavaComponent.getKoin

/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */
class SklandWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(
    context,
    workerParams
) {

    private val prefManager: PrefManager by getKoin().inject()
    override suspend fun doWork(): Result {
        //签到
        try {
            if (prefManager.autoAttendance.get()) {
                val lastAttendanceTs = prefManager.lastAttendanceTs.get()
                val currentTs = TimeUtils.getCurrentTs()
                if (TimeUtils.getDayNum(currentTs) > TimeUtils.getDayNum(lastAttendanceTs)) {
                    val database = ArkDatabase.getDatabase(APP)
                    val accountSkDao = database.getAccountSkDao()
                    val accountList = accountSkDao.getAll()
                    for (account in accountList) {
                        NetWorkTask.sklandAttendance(account)
                    }
                    prefManager.lastAttendanceTs.set(currentTs)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val account = prefManager.baseAccountSk.get()
        if (account.uid == "") {
            return Result.success()
        }
        if (prefManager.powerSavingMode.get()) {
            val now = TimeUtils.getCurrentTs()
            //ap
            // TODO: 删掉这部分。而且写错了
//            val apCache = prefManager.apCache.get()
//            val passTimeAp = now - apCache.lastUpdateTs
//            if (apCache.current >= apCache.max || apCache.remainSec < passTimeAp) {
//                apCache.remainSec = 0L
//            } else {
//                apCache.current += passTimeAp.toInt() / (60 * 6)
//                apCache.remainSec -= passTimeAp
//            }
//            apCache.lastUpdateTs = now
//            prefManager.apCache.set(apCache)

            //labor TODO: labor的代码也是错的。你紫菜吧
            val laborCache = prefManager.laborCache.get()
            val passTimeLabor = now - laborCache.lastSyncTs
            if (laborCache.current >= laborCache.max || laborCache.remainSec < passTimeLabor) {
                laborCache.remainSec = 0L
            } else {
                laborCache.current =
                    (passTimeLabor * (laborCache.max - laborCache.current)
                            / laborCache.remainSec + laborCache.current).toInt()
                laborCache.remainSec -= passTimeLabor
            }
            laborCache.lastSyncTs = now
            prefManager.laborCache.set(laborCache)

        } else {
            try {
                val playerData = getPlayerData(account)
                val apCache = ApCache(
                    playerData.currentTs,
                    playerData.apInfo.remainSecs,
                    playerData.apInfo.recoverTime,
                    playerData.apInfo.max,
                    playerData.apInfo.current,
                    false
                )
                val laborCache = LaborCache(
                    playerData.currentTs,
                    playerData.labor.remainSecs,
                    playerData.labor.max,
                    playerData.labor.current,
                    false
                )
                prefManager.apCache.set(apCache)
                prefManager.laborCache.set(laborCache)
            } catch (e: Exception) {
                e.printStackTrace()
                return Result.failure()
            }
        }
        val appWidgetManager = AppWidgetManager.getInstance(APP)

        // 更新 Widget 1
        val widget1Ids =
            appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, Widget1::class.java))
        widget1Ids.forEach { id ->
            val intent = Intent(applicationContext, Widget1::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(id))
            applicationContext.sendBroadcast(intent)
        }

        // 更新 Widget 2
        val widget2Ids =
            appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, Widget2::class.java))
        widget2Ids.forEach { id ->
            val intent = Intent(applicationContext, Widget2::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(id))
            applicationContext.sendBroadcast(intent)
        }
        // 更新 Widget 3
        val widget3Ids =
            appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, Widget3::class.java))
        widget3Ids.forEach { id ->
            val intent = Intent(applicationContext, Widget3::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(id))
            applicationContext.sendBroadcast(intent)
        }

        val widget4Ids =
            appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, AttendanceWidget::class.java))
        widget4Ids.forEach { id ->
            val intent = Intent(applicationContext, AttendanceWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(id))
            applicationContext.sendBroadcast(intent)
        }
        return Result.success()
    }

    private suspend fun getPlayerData(account: AccountSk): RealTimeData {
        val response = getGameInfoConnectionTaskTest(account)
        if (!response.isSuccessful) throw Exception("!response.isSuccessful")
        response.body() ?: throw Exception("response empty")
        return geneRealTimeData(response.body()!!)
    }
}