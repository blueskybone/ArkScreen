package com.blueskybone.arkscreen.data.local.pref

import com.blueskybone.arkscreen.data.local.pref.preference.Preference
import com.blueskybone.arkscreen.data.local.pref.preference.PreferenceStore
import com.blueskybone.arkscreen.domain.model.cache.ApCache
import com.blueskybone.arkscreen.domain.model.cache.CacheAccountInfo
import com.blueskybone.arkscreen.domain.model.cache.LaborCache
import com.blueskybone.arkscreen.domain.model.cache.MeetCache
import com.blueskybone.arkscreen.domain.model.cache.RecruitCache
import com.blueskybone.arkscreen.domain.model.cache.RefreshCache
import com.blueskybone.arkscreen.domain.model.cache.TrainCache
import java.util.function.Function
import android.util.Base64
import timber.log.Timber

/**
 * Created by blueskybone
 * Date: 2026/3/20
 */

/*专门用于游戏数据缓存的preference*/
/** Short-lived API snapshots used by home cards and widgets. */
class CachePrefManager() {
    constructor(preferenceStore: PreferenceStore) : this() {
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
        accountInfo = preferenceStore.getObject(
            "realtime_cache_account",
            CacheAccountInfo.empty(),
            serializerAccountInfo(),
            deserializerAccountInfo(),
        )
    }

    lateinit var apCache: Preference<ApCache>
    lateinit var laborCache: Preference<LaborCache>
    lateinit var trainCache: Preference<TrainCache>
    lateinit var recruitCache: Preference<RecruitCache>
    lateinit var refreshCache: Preference<RefreshCache>
    lateinit var meetCache: Preference<MeetCache>
    lateinit var accountInfo: Preference<CacheAccountInfo>

    private fun serializerAccountInfo(): (CacheAccountInfo) -> String = { info ->
        val nickname = Base64.encodeToString(
            info.nickname.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP or Base64.URL_SAFE,
        )
        "${info.uid}@$nickname@${info.official}"
    }

    private fun deserializerAccountInfo(): Function<String, CacheAccountInfo> =
        Function { value ->
            runCatching {
                val fields = value.split("@", limit = 3)
                CacheAccountInfo(
                    uid = fields[0],
                    nickname = String(
                        Base64.decode(fields[1], Base64.NO_WRAP or Base64.URL_SAFE),
                        Charsets.UTF_8,
                    ),
                    official = fields[2].toBooleanStrict(),
                )
            }.getOrElse { CacheAccountInfo.empty() }
        }


    private fun serializerAp(): (ApCache) -> String {
        return { cache ->
            "${cache.lastSyncTs}@${cache.remainSec}@${cache.recoverTime}@${cache.max}@${cache.current}@${cache.isnull}"
        }
    }

    private fun deserializerAp(): java.util.function.Function<String, ApCache> {
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
                Timber.w(e, "Failed to decode AP cache")
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
                Timber.w(e, "Failed to decode drone cache")
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
                Timber.w(e, "Failed to decode training cache")
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
                Timber.w(e, "Failed to decode recruitment cache")
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
                Timber.w(e, "Failed to decode recruitment refresh cache")
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
                Timber.w(e, "Failed to decode meeting cache")
                return@Function MeetCache.default()
            }
        }
    }

}
