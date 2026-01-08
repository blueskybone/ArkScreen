package com.blueskybone.arkscreen.preference

import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.playerinfo.cache.ApCache
import com.blueskybone.arkscreen.playerinfo.cache.LaborCache
import com.blueskybone.arkscreen.playerinfo.cache.MeetCache
import com.blueskybone.arkscreen.playerinfo.cache.RecruitCache
import com.blueskybone.arkscreen.playerinfo.cache.RefreshCache
import com.blueskybone.arkscreen.playerinfo.cache.TrainCache
import com.blueskybone.arkscreen.preference.preference.Preference
import com.blueskybone.arkscreen.preference.preference.PreferenceStore
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.ui.bindinginfo.AppTheme
import com.blueskybone.arkscreen.ui.bindinginfo.FloatWindowAppearance
import com.blueskybone.arkscreen.ui.bindinginfo.RecruitMode
import com.blueskybone.arkscreen.ui.bindinginfo.ScDelay
import com.blueskybone.arkscreen.ui.bindinginfo.ScreenshotDelay
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetAlpha
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetContent
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetTextColor
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetUpdateFreq
import com.blueskybone.arkscreen.ui.recyclerview.ViewType
import java.util.function.Function

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
class PrefManager() {
    constructor(preferenceStore: PreferenceStore) : this() {
        recruitMode = preferenceStore.getString(RecruitMode.key, RecruitMode.FLOATWINDOW)
        floatWindowAppearance =
            preferenceStore.getString(FloatWindowAppearance.key, FloatWindowAppearance.COLORFUL)
        screenShotDelay = preferenceStore.getString(ScDelay.key, ScDelay.defaultValue)
        powerSavingMode = preferenceStore.getBoolean("power_saving_mode", false)


        autoAttendance = preferenceStore.getBoolean("auto_attendance", true)
        lastAttendanceTs = preferenceStore.getLong("last_attendance_ts", 0L)
        warnOverlayPermission = preferenceStore.getBoolean("warn_overlay_permission", true)
        autoUpdateApp = preferenceStore.getBoolean("auto_app_update", true)
        timeCorrect = preferenceStore.getBoolean("time_correct", false)
        showHomeAnnounce = preferenceStore.getBoolean("show_home_announce", true)
        timeCorrectSec = preferenceStore.getLong("time_correct_sec", 0L)
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
            LaborCache.default(), serializerLabor(), deserializerLabor()
        )
        trainCache = preferenceStore.getObject(
            "train_cache",
            TrainCache.default(), serializerTrain(), deserializerTrain()
        )
        recruitCache = preferenceStore.getObject(
            "recruit_cache",
            RecruitCache.default(), serializerRecruit(), deserializerRecruit()
        )
        refreshCache = preferenceStore.getObject(
            "refresh_cache",
            RefreshCache.default(), serializerRefresh(), deserializerRefresh()
        )
        meetCache = preferenceStore.getObject(
            "meet_cache",
            MeetCache.default(), serializerMeet(), deserializerMeet()
        )

        insertLink = preferenceStore.getBoolean("insert_link", false)
        backAutoAtd = preferenceStore.getBoolean("back_auto_attendance", false)
        alarmAtdHour = preferenceStore.getInt("alarm_attendance_hour", 0)
        alarmAtdMin = preferenceStore.getInt("alarm_attendance_min", 10)
        useInnerWeb = preferenceStore.getBoolean("use_inner_web", true)
        appTheme = preferenceStore.getString("app_theme", AppTheme.defaultValue)

        widgetAlpha = preferenceStore.getInt(WidgetAlpha.key, WidgetAlpha.defaultValue)
        widgetUpdateFreq = preferenceStore.getString(
            WidgetUpdateFreq.key,
            WidgetUpdateFreq.defaultValue
        )  //更新频率：15min 30min 1h
        widgetTextColor =
            preferenceStore.getString(WidgetTextColor.key, WidgetTextColor.defaultValue)
        widgetBg = preferenceStore.getInt("widget_bg", R.drawable.widget_bg_black)
// Widget 1 初始化
        widget1Size = preferenceStore.getString(WidgetSize.key + "_1", WidgetSize.defaultValue)
        widget1Content = preferenceStore.getString(
            WidgetContent.key + "_1",
            WidgetContent.defaultValue
        )
// Widget 2 初始化
        widget2Size = preferenceStore.getString(WidgetSize.key + "_2", WidgetSize.defaultValue)
        widget2Content = preferenceStore.getString(
            WidgetContent.key + "_2",
            WidgetContent.defaultValue
        )
// Widget 3 初始化
        widget3Size = preferenceStore.getString(WidgetSize.key + "_3", WidgetSize.defaultValue)
        widget3Content1 = preferenceStore.getString(
            WidgetContent.key + "_3_1",
            WidgetContent.defaultValue
        )
        widget3Content2 = preferenceStore.getString(
            WidgetContent.key + "_3_2",
            WidgetContent.defaultValue2
        )

// Widget 4 初始化
        widget4Size = preferenceStore.getString(WidgetSize.key + "_4", WidgetSize.defaultValue)

        widget4ShowRecruit = preferenceStore.getBoolean(
            "widget_4_show_recruit",
            true // 默认显示
        )
        widget4ShowDatabase = preferenceStore.getBoolean(
            "widget_4_show_db",
            true // 默认显示
        )
        widget4ShowTrain = preferenceStore.getBoolean(
            "widget_4_show_train",
            true // 默认显示
        )
        widget4ShowStarter = preferenceStore.getBoolean(
            "widget_4_show_starter",
            true
        )
        assetsViewType = preferenceStore.getInt("assets_view_type", ViewType.GRID.ordinal)
    }

    lateinit var warnOverlayPermission: Preference<Boolean>  //开屏不再提示悬浮窗权限
    lateinit var recruitMode: Preference<String>        //快速公招模式
    lateinit var floatWindowAppearance: Preference<String>  //快速公招结果显示风格（未实装）
    lateinit var screenShotDelay: Preference<String>       //快速公招截图延迟选项
    lateinit var powerSavingMode: Preference<Boolean>   //桌面组件省流模式
    lateinit var autoAttendance: Preference<Boolean>    //桌面组件自动签到
    lateinit var lastAttendanceTs: Preference<Long>     //记录签到时间，避免同一天重复签到

    /*
    * 森空岛时间校准选项
    * */
    lateinit var timeCorrect: Preference<Boolean>
    lateinit var timeCorrectSec: Preference<Long>

    lateinit var baseAccountSk: Preference<AccountSk>   //默认游戏账号
    lateinit var baseAccountGc: Preference<AccountGc>   //默认卡池账号

    /*
    * 省流模式缓存选项
    * */
    lateinit var apCache: Preference<ApCache>
    lateinit var laborCache: Preference<LaborCache>
    lateinit var trainCache: Preference<TrainCache>
    lateinit var recruitCache: Preference<RecruitCache>
    lateinit var refreshCache: Preference<RefreshCache>
    lateinit var meetCache: Preference<MeetCache>

    /*
    * 后台自动签到
    * */
    lateinit var backAutoAtd: Preference<Boolean>
    lateinit var alarmAtdHour: Preference<Int>
    lateinit var alarmAtdMin: Preference<Int>

    /*
    * 其他选项
    * */
    lateinit var autoUpdateApp: Preference<Boolean>
    lateinit var useInnerWeb: Preference<Boolean>
    lateinit var appTheme: Preference<String>
    lateinit var showHomeAnnounce: Preference<Boolean>

    /*
    * 干员资产列表展示模式
    * */
    lateinit var assetsViewType: Preference<Int>

    /*
    * 桌面组件相关设置
    * 遵守高度定制化的方案，对每一个widget单独做一套配置
    * 目前有4个widget
    * */
    lateinit var widgetAlpha: Preference<Int>
    lateinit var widgetUpdateFreq: Preference<String>  //更新频率：15min 30min 1h  (目前该选项未启用)

    //统一配置：文字颜色，背景不透明度，背景图片，
    //单独配置：文字大小，显示内容。
    lateinit var widgetTextColor: Preference<String>
    lateinit var widgetBg: Preference<Int>
    lateinit var widget1Size: Preference<String>
    lateinit var widget1Content: Preference<String>   //4选一

    lateinit var widget2Size: Preference<String>
    lateinit var widget2Content: Preference<String>   //4选一

    lateinit var widget3Size: Preference<String>
    lateinit var widget3Content1: Preference<String>
    lateinit var widget3Content2: Preference<String>

    lateinit var widget4Size: Preference<String>
    lateinit var widget4ShowRecruit: Preference<Boolean>
    lateinit var widget4ShowDatabase: Preference<Boolean>
    lateinit var widget4ShowTrain: Preference<Boolean>
    lateinit var widget4ShowStarter: Preference<Boolean>


    //new
    //lateinit var recruitPageShowMode: Preference<String> // 公招悬浮窗显示风格：simple or complex , default is complex


    //用于预输入数据标识，一次有效
    lateinit var insertLink: Preference<Boolean>

    /*
    * object Preference 的编解码器
    * */
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
        return { cache ->
            "${cache.lastSyncTs}@${cache.remainSec}@${cache.recoverTime}@${cache.max}@${cache.current}@${cache.isnull}"
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
                    list[3].toInt(),
                    list[4].toInt(),
                    list[5].toBoolean()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@Function ApCache.default()
            }
        }
    }

    private fun serializerLabor(): (LaborCache) -> String {
        return { cache ->
            "${cache.lastSyncTs}@${cache.remainSec}@${cache.max}@${cache.current}@${cache.isnull}"
        }
    }

    private fun deserializerLabor(): Function<String, LaborCache> {
        return Function { string: String ->
            try {
                val list =
                    string.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                return@Function LaborCache(
                    list[0].toLong(),
                    list[1].toLong(),
                    list[2].toInt(),
                    list[3].toInt(),
                    list[4].toBoolean()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@Function LaborCache.default()
            }
        }
    }


    private fun serializerTrain(): (TrainCache) -> String {
        return { cache ->
            "${cache.lastSyncTs}@${cache.trainee}@${cache.status}@${cache.completeTime}@${cache.isnull}"
        }
    }

    private fun deserializerTrain(): Function<String, TrainCache> {
        return Function { string: String ->
            try {
                val list =
                    string.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                return@Function TrainCache(
                    list[0].toLong(),
                    list[1],
                    list[2].toLong(),
                    list[3].toLong(),
                    list[4].toBoolean()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@Function TrainCache.default()
            }
        }
    }

    private fun serializerRecruit(): (RecruitCache) -> String {
        return { cache ->
            "${cache.lastSyncTs}@${cache.max}@${cache.complete}@${cache.completeTime}@${cache.isNull}"
        }
    }

    private fun deserializerRecruit(): Function<String, RecruitCache> {
        return Function { string: String ->
            try {
                val list =
                    string.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                return@Function RecruitCache(
                    list[0].toLong(),
                    list[1].toInt(),
                    list[2].toInt(),
                    list[3].toLong(),
                    list[4].toBoolean()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@Function RecruitCache.default()
            }
        }
    }

    private fun serializerRefresh(): (RefreshCache) -> String {
        return { cache ->
            "${cache.lastSyncTs}@${cache.max}@${cache.count}@${cache.completeTime}@${cache.isNull}"
        }
    }

    private fun deserializerRefresh(): Function<String, RefreshCache> {
        return Function { string: String ->
            try {
                val list =
                    string.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                return@Function RefreshCache(
                    list[0].toLong(),
                    list[1].toInt(),
                    list[2].toInt(),
                    list[3].toLong(),
                    list[4].toBoolean()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@Function RefreshCache.default()
            }
        }
    }

    private fun serializerMeet(): (MeetCache) -> String {
        return { cache ->
            "${cache.lastSyncTs}@${cache.completeTime}@${cache.stats}@${cache.isnull}"
        }
    }

    private fun deserializerMeet(): Function<String, MeetCache> {
        return Function { string: String ->
            try {
                val list =
                    string.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                return@Function MeetCache(
                    list[0].toLong(),
                    list[1].toLong(),
                    list[2].toInt(),
                    list[3].toBoolean()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@Function MeetCache.default()
            }
        }
    }


    private fun serializerGc(): Function<AccountGc, String> {
        return Function<AccountGc, String> { account: AccountGc ->
            account.uid +
                    "@" + account.channelMasterId +
                    "@" + account.nickName +
                    "@" + account.token +
                    "@" + account.official +
                    "@" + account.akUserCenter +
                    "@" + account.xrToken
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
                    list[4].toBoolean(),
                    list[5],
                    list[6]
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@Function AccountGc.default()
            }
        }
    }
}