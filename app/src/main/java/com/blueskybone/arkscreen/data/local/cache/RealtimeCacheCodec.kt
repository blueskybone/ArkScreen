package com.blueskybone.arkscreen.data.local.cache

import com.blueskybone.arkscreen.domain.model.cache.*
import org.json.JSONObject

/** Explicit fields keep this format stable under R8. */
object RealtimeCacheCodec {
    fun encode(value: RealtimeCacheSnapshot): String = JSONObject().apply {
        put("accountInfo", JSONObject().apply {
            put("uid", value.accountInfo.uid)
            put("nickname", value.accountInfo.nickname)
            put("official", value.accountInfo.official)
        })
        put("apCache", JSONObject().apply {
            put("lastSyncTs", value.apCache.lastSyncTs)
            put("remainSec", value.apCache.remainSec)
            put("recoverTime", value.apCache.recoverTime)
            put("max", value.apCache.max)
            put("current", value.apCache.current)
            put("isnull", value.apCache.isnull)
        })
        put("laborCache", JSONObject().apply {
            put("lastSyncTs", value.laborCache.lastSyncTs)
            put("remainSec", value.laborCache.remainSec)
            put("max", value.laborCache.max)
            put("current", value.laborCache.current)
            put("isnull", value.laborCache.isnull)
        })
        put("trainCache", JSONObject().apply {
            put("lastSyncTs", value.trainCache.lastSyncTs)
            put("trainee", value.trainCache.trainee)
            put("status", value.trainCache.status)
            put("completeTime", value.trainCache.completeTime)
            put("isnull", value.trainCache.isnull)
        })
        put("recruitCache", JSONObject().apply {
            put("lastSyncTs", value.recruitCache.lastSyncTs)
            put("max", value.recruitCache.max)
            put("complete", value.recruitCache.complete)
            put("completeTime", value.recruitCache.completeTime)
            put("isNull", value.recruitCache.isNull)
        })
        put("refreshCache", JSONObject().apply {
            put("lastSyncTs", value.refreshCache.lastSyncTs)
            put("max", value.refreshCache.max)
            put("count", value.refreshCache.count)
            put("completeTime", value.refreshCache.completeTime)
            put("isNull", value.refreshCache.isNull)
        })
        put("meetCache", JSONObject().apply {
            put("lastSyncTs", value.meetCache.lastSyncTs)
            put("completeTime", value.meetCache.completeTime)
            put("stats", value.meetCache.stats)
            put("isnull", value.meetCache.isnull)
        })
    }.toString()

    fun decode(payload: String): RealtimeCacheSnapshot {
        val root = JSONObject(payload)
        return RealtimeCacheSnapshot(
            accountInfo = root.getJSONObject("accountInfo").let {
                CacheAccountInfo(
                    uid = it.getString("uid"),
                    nickname = it.getString("nickname"),
                    official = it.getBoolean("official"),
                )
            },
            apCache = root.getJSONObject("apCache").let {
                ApCache(
                    lastSyncTs = it.getLong("lastSyncTs"),
                    remainSec = it.getLong("remainSec"),
                    recoverTime = it.getLong("recoverTime"),
                    max = it.getInt("max"),
                    current = it.getInt("current"),
                    isnull = it.getBoolean("isnull"),
                )
            },
            laborCache = root.getJSONObject("laborCache").let {
                LaborCache(
                    lastSyncTs = it.getLong("lastSyncTs"),
                    remainSec = it.getLong("remainSec"),
                    max = it.getInt("max"),
                    current = it.getInt("current"),
                    isnull = it.getBoolean("isnull"),
                )
            },
            trainCache = root.getJSONObject("trainCache").let {
                TrainCache(
                    lastSyncTs = it.getLong("lastSyncTs"),
                    trainee = it.getString("trainee"),
                    status = it.getLong("status"),
                    completeTime = it.getLong("completeTime"),
                    isnull = it.getBoolean("isnull"),
                )
            },
            recruitCache = root.getJSONObject("recruitCache").let {
                RecruitCache(
                    lastSyncTs = it.getLong("lastSyncTs"),
                    max = it.getInt("max"),
                    complete = it.getInt("complete"),
                    completeTime = it.getLong("completeTime"),
                    isNull = it.getBoolean("isNull"),
                )
            },
            refreshCache = root.getJSONObject("refreshCache").let {
                RefreshCache(
                    lastSyncTs = it.getLong("lastSyncTs"),
                    max = it.getInt("max"),
                    count = it.getInt("count"),
                    completeTime = it.getLong("completeTime"),
                    isNull = it.getBoolean("isNull"),
                )
            },
            meetCache = root.getJSONObject("meetCache").let {
                MeetCache(
                    lastSyncTs = it.getLong("lastSyncTs"),
                    completeTime = it.getLong("completeTime"),
                    stats = it.getInt("stats"),
                    isnull = it.getBoolean("isnull"),
                )
            },
        )
    }
}
