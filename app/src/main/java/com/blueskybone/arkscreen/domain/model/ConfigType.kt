package com.blueskybone.arkscreen.domain.model

/**
 * Created by blueskybone
 * Date: 2026/3/9
 */
// 热更新业务配置
enum class ConfigType(val fileName: String, val xmlUrl: String) {
    CHAR_MAP("char_info_map.json", "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/char_info_map_version.xml"),
    RECRUIT_DB("recruit_db.json", "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/recruit_version.xml"),
    I18N_DB("i18n.json", "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/i18n_version.xml"),
    APP_INFO("app_info.xml","https://gitee.com/blueskybone/ArkScreen/raw/master/resource/app_version.xml")
}