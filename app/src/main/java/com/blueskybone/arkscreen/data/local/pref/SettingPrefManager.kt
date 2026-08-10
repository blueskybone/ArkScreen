package com.blueskybone.arkscreen.data.local.pref

import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.preference.Preference
import com.blueskybone.arkscreen.data.local.pref.preference.PreferenceStore
import com.blueskybone.arkscreen.data.local.room.AccountSk
import com.blueskybone.arkscreen.domain.model.cache.ApCache
import com.blueskybone.arkscreen.ui.common.bindinginfo.AppTheme
import com.blueskybone.arkscreen.ui.common.bindinginfo.FloatWindowAppearance
import com.blueskybone.arkscreen.ui.common.bindinginfo.RecruitMode
import com.blueskybone.arkscreen.ui.common.bindinginfo.ScDelay
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetAlpha
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetContent
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetTextColor
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetUpdateFreq

/**
 * Created by blueskybone
 * Date: 2026/3/20
 */
/*用户可操作的配置*/
class SettingPrefManager() {
    constructor(preferenceStore: PreferenceStore) : this() {
        recruitMode = preferenceStore.getString(RecruitMode.key, RecruitMode.FLOATWINDOW)
        floatWindowAppearance =
            preferenceStore.getString(FloatWindowAppearance.key, FloatWindowAppearance.COLORFUL)
        screenShotDelay = preferenceStore.getString(ScDelay.key, ScDelay.defaultValue)
        powerSavingMode = preferenceStore.getBoolean("power_saving_mode", false)


        autoAttendance = preferenceStore.getBoolean("auto_attendance", true)

        autoUpdateApp = preferenceStore.getBoolean("auto_app_update", true)
        timeCorrect = preferenceStore.getBoolean("time_correct", false)
        showHomeAnnounce = preferenceStore.getBoolean("show_home_announce", true)


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
    }


    lateinit var recruitMode: Preference<String>        //快速公招模式
    lateinit var floatWindowAppearance: Preference<String>  //快速公招结果显示风格（未实装）
    lateinit var screenShotDelay: Preference<String>       //快速公招截图延迟选项
    lateinit var powerSavingMode: Preference<Boolean>   //桌面组件省流模式
    lateinit var autoAttendance: Preference<Boolean>    //桌面组件自动签到

    /*
    * 森空岛时间校准选项
    * */
    lateinit var timeCorrect: Preference<Boolean>   //开启时间校正

    lateinit var baseAccountSk: Preference<AccountSk>   //默认游戏账号（删掉）

    /*
    * 省流模式缓存选项
    * */
    lateinit var apCache: Preference<ApCache>   //（删掉）

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
    lateinit var showHomeAnnounce: Preference<Boolean>  //显示首页公告

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

}