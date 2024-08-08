package com.blueskybone.arkscreen.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.base.PrefManager
import com.blueskybone.arkscreen.base.PrefManager.WidgetTheme
import com.blueskybone.arkscreen.base.data.AccountSk
import com.blueskybone.arkscreen.base.data.PlayerData
import com.blueskybone.arkscreen.network.NetWorkTask
import com.blueskybone.arkscreen.util.TimeUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.java.KoinJavaComponent.getKoin
import java.util.zip.GZIPInputStream

/**
 *   Created by blueskybone
 *   Date: 2024/8/7
 */
class SklandWorker(context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {

        val context = applicationContext
        val views = RemoteViews(context.packageName, R.layout.skland_widget)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, SklandWidget::class.java)

        //修改layout
        //字体颜色，背景颜色，背景透明度
        val prefManager: PrefManager by getKoin().inject()
        views.setImageViewResource(R.id.widget_skland_bg, R.drawable.widget_background);
        views.setInt(R.id.widget_skland_bg, "setAlpha", prefManager.widgetAlpha.get())
        when (prefManager.widgetThemePreference.get()) {
            WidgetTheme.WHITE_ON_BLACK -> {
                views.setInt(R.id.widget_skland_bg, "setColorFilter", Color.BLACK)
                views.setInt(R.id.widget_text, "setTextColor", Color.WHITE)
            }

            else -> {
                views.setInt(R.id.widget_skland_bg, "setColorFilter", Color.WHITE)
                views.setInt(R.id.widget_text, "setTextColor", Color.BLACK)
            }
        }

        //checkAttendance
        //偷隔壁的
        try {
            if (prefManager.isAutoCheckOn.get()) {
                val lastAttendanceTs = prefManager.LastAttendanceTs.get()
                val currentTs = TimeUtils.getCurrentTs()
                if (TimeUtils.getDayNum(currentTs) > TimeUtils.getDayNum(lastAttendanceTs)) {
                    val accountList = prefManager.ListAccountSk.get()
                    for (account in accountList) {
                        NetWorkTask.sklandAttendance(
                            account.token,
                            account.uid,
                            account.channelMasterId
                        )
                    }
                    prefManager.LastAttendanceTs.set(currentTs)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        //requestForAp
        try {
            val account = prefManager.BaseAccount.get()
            if (account == null) {
                views.setTextViewText(R.id.widget_text, "未登录")
                return Result.success()
            }
            val playerData = getPlayerData(account)
            val apText = playerData.apInfo.current.toString() + " / " + playerData.apInfo.max
            views.setTextViewText(R.id.widget_text, apText)
        } catch (e: Exception) {
            e.printStackTrace()
            val text = e.message
            views.setTextViewText(R.id.widget_text, text)

        } finally {
            Handler(Looper.getMainLooper()).post {
                appWidgetManager.updateAppWidget(componentName, views)
            }
        }
        return Result.success()

    }

    private fun getPlayerData(account: AccountSk): PlayerData {
        val cn = NetWorkTask.getGameInfoInputConnection(account) ?: throw Exception("连接失败")
        val inputStream = cn.inputStream
        val gzip = GZIPInputStream(inputStream)
        val om = ObjectMapper()
        val result = com.blueskybone.arkscreen.util.getPlayerData(om.readTree(gzip))
        gzip.close()
        inputStream.close()
        cn.disconnect()
        return result
    }

}