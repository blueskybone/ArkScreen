package com.blueskybone.arkscreen.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.bindinginfo.WidgetAppearance
import com.blueskybone.arkscreen.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.network.NetWorkTask
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.getGameInfoConnectionTask
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.receiver.WidgetReceiver
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.ApCache
import com.blueskybone.arkscreen.room.ArkDatabase
import com.blueskybone.arkscreen.room.Operator
import com.blueskybone.arkscreen.room.RealTimeData
import com.blueskybone.arkscreen.util.TimeUtils
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.getKoin
import java.util.zip.GZIPInputStream

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
            val passTimeLabor = now - laborCache.lastUpdateTs
            if (laborCache.current >= laborCache.max || laborCache.remainSec < passTimeLabor) {
                laborCache.remainSec = 0L
            } else {
                laborCache.current =
                    (passTimeLabor * (laborCache.max - laborCache.current)
                            / laborCache.remainSec + laborCache.current).toInt()
                laborCache.remainSec -= passTimeLabor
            }
            laborCache.lastUpdateTs = now
            prefManager.laborCache.set(laborCache)

        } else {
            try{
                val playerData = getPlayerData(account)
                val apCache = ApCache(
                    playerData.currentTs,
                    playerData.currentTs,
                    playerData.apInfo.remainSecs,
                    playerData.apInfo.recoverTime,
                    playerData.apInfo.max,
                    playerData.apInfo.current,
                    false
                )
                val laborCache = ApCache(
                    playerData.currentTs,
                    playerData.currentTs,
                    playerData.labor.remainSecs,
                    playerData.labor.recoverTime,
                    playerData.labor.max,
                    playerData.labor.current,
                    false
                )
                prefManager.apCache.set(apCache)
                prefManager.laborCache.set(laborCache)
            }catch (e:Exception){
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
        return Result.success()
    }

    private suspend fun getPlayerData(account:AccountSk): RealTimeData {
        val response = getGameInfoConnectionTask(account)
        response.body()?.use{ body ->
            val gzip = GZIPInputStream(body.byteStream())
            val result = readRealTimeFromGzip(ObjectMapper().readTree(gzip))
            return result
        }?: throw Exception("response body is empty")
    }

    private fun readRealTimeFromGzip(tree: JsonNode): RealTimeData {
        val currentTs = tree.at("/data/currentTs").asLong()
        val playerData = RealTimeData()
        playerData.currentTs = currentTs
        //status
        run {
            playerData.playerStatus.uid = tree.at("/data/status/uid").asText()
            playerData.playerStatus.nickname = tree.at("/data/status/name").asText()
            playerData.playerStatus.level = tree.at("/data/status/level").asInt()
            playerData.playerStatus.registerTs = tree.at("/data/status/registerTs").asLong()
            playerData.playerStatus.lastOnlineTs = tree.at("/data/status/lastOnlineTs").asLong()
        }
        //ap
        run {
            val current: Int = tree.at("/data/status/ap/current").asInt()
            val max: Int = tree.at("/data/status/ap/max").asInt()
            val lastApAddTime: Long = tree.at("/data/status/ap/lastApAddTime").asLong()
            val recoverTime: Long = tree.at("/data/status/ap/completeRecoveryTime").asLong()

            playerData.apInfo.max = max

            playerData.apInfo.remainSecs = -1L
            playerData.apInfo.recoverTime = -1L

            when {
                current >= max -> {
                    playerData.apInfo.current = current
                }

                recoverTime < currentTs -> {
                    playerData.apInfo.current = max
                }

                else -> {
                    val elapsedTime = (currentTs - lastApAddTime).toInt() / (60 * 6)
                    playerData.apInfo.current = current + elapsedTime
                    playerData.apInfo.remainSecs = recoverTime - currentTs
                    playerData.apInfo.recoverTime = recoverTime
                }
            }
        }

        //labor
        run {
            val laborNode = tree.at("/data/building/labor")

            // 获取基本值
            val laborValue = laborNode["value"].asInt()
            val laborMax = laborNode["maxValue"].asInt()
            val remainSecs = laborNode["remainSecs"].asLong()
            val lastUpdateTime = laborNode["lastUpdateTime"].asLong()

            val laborCurrent = if (remainSecs == 0L) {
                laborValue
            } else {
                val elapsedTime = currentTs - lastUpdateTime
                ((elapsedTime * (laborMax - laborValue) / remainSecs) + laborValue).coerceAtMost(
                    laborMax.toLong()
                ).toInt()
            }
            val laborRemain = remainSecs - (currentTs - lastUpdateTime).toInt().coerceAtLeast(0)
            val recoverTime = remainSecs + lastUpdateTime

            playerData.labor.apply {
                this.current = laborCurrent
                this.max = laborMax
                this.remainSecs = laborRemain
                this.recoverTime = recoverTime
            }
        }
        return playerData
    }
}