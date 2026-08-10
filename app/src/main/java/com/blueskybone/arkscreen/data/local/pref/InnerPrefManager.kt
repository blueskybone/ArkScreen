package com.blueskybone.arkscreen.data.local.pref

import com.blueskybone.arkscreen.data.local.pref.preference.Preference
import com.blueskybone.arkscreen.data.local.pref.preference.PreferenceStore
import com.blueskybone.arkscreen.ui.character.adapter.ViewType


/**
 * Created by blueskybone
 * Date: 2026/3/20
 */
/*内部使用的preference，用户不可见*/
class InnerPrefManager() {
    constructor(preferenceStore: PreferenceStore) : this() {
        warnOverlayPermission = preferenceStore.getBoolean("warn_overlay_permission", true)

        currentAccountSkUid = preferenceStore.getString("default_acc_sk_uid", "")
        currentAccountGcUid = preferenceStore.getString("default_acc_gc_uid", "")

        lastCheckTs = preferenceStore.getLong("last_check_ts", 0L)
        timeCorrectSec = preferenceStore.getLong("time_correct_sec", 0L)

        insertLink = preferenceStore.getBoolean("insert_link", false)

        assetsViewType = preferenceStore.getInt("assets_view_type", ViewType.GRID.ordinal)
    }

    lateinit var warnOverlayPermission: Preference<Boolean>  //开屏不再提示悬浮窗权限
    lateinit var currentAccountSkUid: Preference<String>        //不再deepcopy，只记录uid，自己去找
    lateinit var currentAccountGcUid: Preference<String>        //不再deepcopy，只记录uid，自己去找
    lateinit var lastCheckTs: Preference<Long>     //记录签到时间，避免同一天重复签到
    lateinit var timeCorrectSec: Preference<Long>  //时间校正
    lateinit var insertLink: Preference<Boolean>  //用于link预输入数据标识，一次有效
    lateinit var assetsViewType: Preference<Int> //干员资产列表展示模式
}