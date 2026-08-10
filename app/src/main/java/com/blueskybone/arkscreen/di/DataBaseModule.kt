package com.blueskybone.arkscreen.di

import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.data.local.pref.CachePrefManager
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.data.local.pref.preference.shared.SharedPreferenceStore
import com.blueskybone.arkscreen.data.local.room.ArkDatabase
import org.koin.dsl.module

val databaseModule = module {

    single { ArkDatabase.getDatabase(APP) }
    single { get<ArkDatabase>().getAccountSkDao() }
    single { get<ArkDatabase>().getAccountGcDao() }
    single { get<ArkDatabase>().getLinkDao() }
    single { get<ArkDatabase>().getAccountEfDao() }
    single { get<ArkDatabase>().getGachaDao() }
}

val preferenceModule = module {
    single { SharedPreferenceStore(APP) }
    single { InnerPrefManager(get<SharedPreferenceStore>()) }
    single { SettingPrefManager(get<SharedPreferenceStore>()) }
    single { CachePrefManager(get<SharedPreferenceStore>()) }
}
