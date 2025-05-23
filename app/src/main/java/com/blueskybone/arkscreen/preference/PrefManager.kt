package com.blueskybone.arkscreen.preference

import com.blueskybone.arkscreen.bindinginfo.FloatWindowAppearance
import com.blueskybone.arkscreen.bindinginfo.RecruitMode
import com.blueskybone.arkscreen.bindinginfo.ScreenshotDelay
import com.blueskybone.arkscreen.bindinginfo.WidgetAlpha
import com.blueskybone.arkscreen.bindinginfo.WidgetAppearance
import com.blueskybone.arkscreen.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.bindinginfo.AppTheme
import com.blueskybone.arkscreen.preference.preference.Preference
import com.blueskybone.arkscreen.preference.preference.PreferenceStore
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.ApCache
import java.util.function.Function

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
class PrefManager() {
    constructor(preferenceStore: PreferenceStore) : this() {
        recruitMode = preferenceStore.getString(RecruitMode.key, RecruitMode.floatWindow)
        floatWindowAppearance =
            preferenceStore.getString(FloatWindowAppearance.key, FloatWindowAppearance.colorful)
        screenShotDelay = preferenceStore.getInt(ScreenshotDelay.key, ScreenshotDelay.defaultValue)
        powerSavingMode = preferenceStore.getBoolean("power_saving_mode", false)
        widgetAppearance =
            preferenceStore.getString(WidgetAppearance.key, WidgetAppearance.defaultValue)
        widgetAlpha = preferenceStore.getInt(WidgetAlpha.key, WidgetAlpha.defaultValue)
        widgetContentSize = preferenceStore.getString(WidgetSize.key, WidgetSize.defaultValue)
        autoAttendance = preferenceStore.getBoolean("auto_attendance", true)
        lastAttendanceTs = preferenceStore.getLong("last_attendance_ts", 0L)
        warnOverlayPermission = preferenceStore.getBoolean("warn_overlay_permission", true)
//        realTimePageAttendance = preferenceStore.getBoolean("real_time_page_attendance", false)
//        realTimePageShowStarter = preferenceStore.getBoolean("real_time_page_show_starter", true)
        autoUpdateApp = preferenceStore.getBoolean("auto_app_update", true)
        timeCorrect = preferenceStore.getBoolean("time_correct", false)
//        debugMode = preferenceStore.getBoolean("debug_mode", false)
        showHomeAnnounce = preferenceStore.getBoolean("show_home_announce", true)
        timeCorrectSec = preferenceStore.getInt("time_correct_sec", 0)
        baseAccountSk = preferenceStore.getObject(
            "base_account_sk",
            AccountSk.default(),
            serializerSk(),
            deserializerSk()
        )
        baseAccountGc = preferenceStore.getObject(
            "base_account_gc",
            AccountGc.default(),
            serializerGc(),
            deserializerGc()
        )
        apCache = preferenceStore.getObject(
            "ap_cache",
            ApCache.default(), serializerAp(), deserializerAp()
        )
        laborCache = preferenceStore.getObject(
            "labor_cache",
            ApCache.default(), serializerAp(), deserializerAp()
        )
        insertLink = preferenceStore.getBoolean("insert_link", false)
        backAutoAtd = preferenceStore.getBoolean("back_auto_attendance", false)
        alarmAtdHour = preferenceStore.getInt("alarm_attendance_hour", 0)
        alarmAtdMin = preferenceStore.getInt("alarm_attendance_min", 10)
        useInnerWeb = preferenceStore.getBoolean("use_inner_web", true)
        appTheme = preferenceStore.getString("app_theme", AppTheme.defaultValue)
    }

    lateinit var warnOverlayPermission: Preference<Boolean>
    lateinit var recruitMode: Preference<String>
    lateinit var floatWindowAppearance: Preference<String>
    lateinit var screenShotDelay: Preference<Int>
    lateinit var powerSavingMode: Preference<Boolean>
    lateinit var widgetAppearance: Preference<String>
    lateinit var widgetAlpha: Preference<Int>
    lateinit var widgetContentSize: Preference<String>
    lateinit var autoAttendance: Preference<Boolean>
    lateinit var lastAttendanceTs: Preference<Long>

    //    lateinit var realTimePageAttendance: Preference<Boolean>
//    lateinit var realTimePageShowStarter: Preference<Boolean>
    lateinit var autoUpdateApp: Preference<Boolean>
    lateinit var timeCorrect: Preference<Boolean>

    //    lateinit var debugMode: Preference<Boolean>
    lateinit var timeCorrectSec: Preference<Int>
    lateinit var baseAccountSk: Preference<AccountSk>
    lateinit var baseAccountGc: Preference<AccountGc>
    lateinit var apCache: Preference<ApCache>
    lateinit var laborCache: Preference<ApCache>
    lateinit var backAutoAtd: Preference<Boolean>
    lateinit var alarmAtdHour: Preference<Int>
    lateinit var alarmAtdMin: Preference<Int>
    lateinit var useInnerWeb: Preference<Boolean>
    lateinit var appTheme: Preference<String>
    lateinit var showHomeAnnounce: Preference<Boolean>

    //用于预输入数据标识，一次有效
    lateinit var insertLink: Preference<Boolean>

    private fun serializerSk(): Function<AccountSk, String> {
        return Function<AccountSk, String> { account: AccountSk ->
            account.uid +
                    "@" + account.channelMasterId +
                    "@" + account.nickName +
                    "@" + account.token +
                    "@" + account.dId +
                    "@" + account.official
        }
    }

    private fun deserializerSk(): Function<String, AccountSk> {
        return Function { string: String ->
            try {
                val list =
                    string.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                return@Function AccountSk(
                    -1L,
                    list[0],
                    list[1],
                    list[2],
                    list[3],
                    list[4],
                    list[5].toBoolean()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@Function AccountSk.default()
            }
        }
    }

    private fun serializerAp(): (ApCache) -> String {
        return { apCache ->
            "${apCache.lastUpdateTs}@${apCache.lastSyncTs}@${apCache.remainSec}@${apCache.recoverTime}@${apCache.max}@${apCache.current}@${apCache.isnull}"
        }
    }

    private fun deserializerAp(): Function<String, ApCache> {
        return Function { string: String ->
            try {
                val list =
                    string.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                return@Function ApCache(
                    list[0].toLong(),
                    list[1].toLong(),
                    list[2].toLong(),
                    list[3].toLong(),
                    list[4].toInt(),
                    list[5].toInt(),
                    list[6].toBoolean()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@Function ApCache.default()
            }
        }


    }

    private fun serializerGc(): Function<AccountGc, String> {
        return Function<AccountGc, String> { account: AccountGc ->
            account.uid +
                    "@" + account.channelMasterId +
                    "@" + account.nickName +
                    "@" + account.token +
                    "@" + account.official
        }
    }

    private fun deserializerGc(): Function<String, AccountGc> {
        return Function { string: String ->
            try {
                val list =
                    string.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                return@Function AccountGc(
                    -1L,
                    list[0],
                    list[1].toInt(),
                    list[2],
                    list[3],
                    list[4].toBoolean()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@Function AccountGc.default()
            }
        }
    }
}