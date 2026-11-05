package com.blueskybone.arkscreen.data.local.pref

import com.blueskybone.arkscreen.data.local.pref.preference.Preference
import com.blueskybone.arkscreen.data.local.pref.preference.PreferenceStore
import com.blueskybone.arkscreen.ui.common.bindinginfo.AppTheme
import com.blueskybone.arkscreen.ui.common.bindinginfo.FloatWindowAppearance
import com.blueskybone.arkscreen.ui.common.bindinginfo.RecruitMode
import com.blueskybone.arkscreen.ui.common.bindinginfo.ScDelay
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetUpdateFreq

/** 用户可见的设置；运行状态和接口缓存不应存放在这里。 */
class SettingPrefManager() {
    constructor(preferenceStore: PreferenceStore) : this() {
        recruitMode = preferenceStore.getString(RecruitMode.key, RecruitMode.FLOATWINDOW)
        floatWindowAppearance =
            preferenceStore.getString(FloatWindowAppearance.key, FloatWindowAppearance.COLORFUL)
        screenShotDelay = preferenceStore.getString(ScDelay.key, ScDelay.defaultValue)
        powerSavingMode = preferenceStore.getBoolean("power_saving_mode", false)


        autoUpdateApp = preferenceStore.getBoolean("auto_app_update", true)
        timeCorrect = preferenceStore.getBoolean("time_correct", false)
        showHomeAnnounce = preferenceStore.getBoolean("show_home_announce", true)


        backAutoAtd = preferenceStore.getBoolean("back_auto_attendance", false)
        alarmAtdHour = preferenceStore.getInt("alarm_attendance_hour", 0)
        alarmAtdMin = preferenceStore.getInt("alarm_attendance_min", 10)
        useInnerWeb = preferenceStore.getBoolean("use_inner_web", true)
        appTheme = preferenceStore.getString("app_theme", AppTheme.defaultValue)
        defaultHomeGame = preferenceStore.getString("default_home_game", "ARKNIGHTS")
        assetsViewType = preferenceStore.getInt("assets_view_type", 0)
        showEmptyGachaPools = preferenceStore.getBoolean("show_empty_gacha_pools", true)
        recoveryNotificationsEnabled = preferenceStore.getBoolean("recovery_notifications_enabled", false)
        notifyApFull = preferenceStore.getBoolean("notify_ap_full", true)
        notifyApEarly = preferenceStore.getBoolean("notify_ap_early", false)
        notifyApEarlyMinutes = preferenceStore.getInt("notify_ap_early_minutes", 30)
        notifyTrainingComplete = preferenceStore.getBoolean("notify_training_complete", false)
        notifyTrainingShift = preferenceStore.getBoolean("notify_training_shift", false)
        notifyTrainingShiftMinutes = preferenceStore.getInt("notify_training_shift_minutes", 30)
        notifyLaborFull = preferenceStore.getBoolean("notify_labor_full", false)
        notifyMeetingComplete = preferenceStore.getBoolean("notify_meeting_complete", false)
        notifyRecruitComplete = preferenceStore.getBoolean("notify_recruit_complete", false)
        notifyRecruitRefresh = preferenceStore.getBoolean("notify_recruit_refresh", false)


        widgetUpdateFreq = preferenceStore.getString(
            WidgetUpdateFreq.key,
            WidgetUpdateFreq.defaultValue
        )  //更新频率：15min 30min 1h
    }


    lateinit var recruitMode: Preference<String>        //快速公招模式
    lateinit var floatWindowAppearance: Preference<String>  //快速公招结果显示风格（未实装）
    lateinit var screenShotDelay: Preference<String>       //快速公招截图延迟选项
    lateinit var powerSavingMode: Preference<Boolean>   //桌面组件省流模式
    /*
    * 森空岛时间校准选项
    * */
    lateinit var timeCorrect: Preference<Boolean>   //开启时间校正

    /*
    * 后台自动签到
    * */
    lateinit var backAutoAtd: Preference<Boolean>   //开启后台签到
    lateinit var alarmAtdHour: Preference<Int>      //后台签到设定h
    lateinit var alarmAtdMin: Preference<Int>       //后台签到设定min

    /*
    * 其他选项
    * */
    lateinit var autoUpdateApp: Preference<Boolean>     //自动检查更新
    lateinit var useInnerWeb: Preference<Boolean>       //使用内置浏览器
    lateinit var appTheme: Preference<String>           //主题
    lateinit var defaultHomeGame: Preference<String>    //启动时显示的游戏
    lateinit var showHomeAnnounce: Preference<Boolean>  //显示首页公告
    lateinit var assetsViewType: Preference<Int>        //干员资产列表展示模式
    lateinit var showEmptyGachaPools: Preference<Boolean>
    lateinit var recoveryNotificationsEnabled: Preference<Boolean>
    lateinit var notifyApFull: Preference<Boolean>
    lateinit var notifyApEarly: Preference<Boolean>
    lateinit var notifyApEarlyMinutes: Preference<Int>
    lateinit var notifyTrainingComplete: Preference<Boolean>
    lateinit var notifyTrainingShift: Preference<Boolean>
    lateinit var notifyTrainingShiftMinutes: Preference<Int>
    lateinit var notifyLaborFull: Preference<Boolean>
    lateinit var notifyMeetingComplete: Preference<Boolean>
    lateinit var notifyRecruitComplete: Preference<Boolean>
    lateinit var notifyRecruitRefresh: Preference<Boolean>

    lateinit var widgetUpdateFreq: Preference<String>  // 更新频率：15 分钟、30 分钟或 1 小时

}
