package com.blueskybone.arkscreen.data.local.cache

import com.blueskybone.arkscreen.data.local.pref.CachePrefManager
import com.blueskybone.arkscreen.data.local.room.RealtimeCacheEntity
import com.blueskybone.arkscreen.data.local.room.dao.RealtimeCacheDao
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.cache.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

class RealtimeCacheStore(private val dao: RealtimeCacheDao, private val legacy: CachePrefManager) {
    private val migrationLock = Mutex()

    suspend fun read(account: AccountSk): RealtimeCacheSnapshot {
        migrateLegacy(account)
        val row = dao.get("arknights", account.channelMasterId, account.uid)
        return row?.takeIf { it.schemaVersion == 1 }?.let {
            runCatching { RealtimeCacheCodec.decode(it.payload) }.getOrNull()
        } ?: RealtimeCacheSnapshot()
    }

    suspend fun write(account: AccountSk, snapshot: RealtimeCacheSnapshot) {
        dao.putIfNewer(RealtimeCacheEntity("arknights", account.channelMasterId, account.uid,
            System.currentTimeMillis() / 1000, snapshot.apCache.lastSyncTs,
            payload = RealtimeCacheCodec.encode(snapshot)))
    }

    private suspend fun migrateLegacy(account: AccountSk) = migrationLock.withLock {
        val owner = legacy.accountInfo.get()
        if (owner.uid != account.uid || owner.official != account.official) return@withLock
        val snapshot = RealtimeCacheSnapshot(owner, legacy.apCache.get(), legacy.laborCache.get(),
            legacy.trainCache.get(), legacy.recruitCache.get(), legacy.refreshCache.get(), legacy.meetCache.get())
        dao.importLegacy(RealtimeCacheEntity("arknights", account.channelMasterId, account.uid,
            snapshot.apCache.lastSyncTs, snapshot.apCache.lastSyncTs, payload = RealtimeCacheCodec.encode(snapshot)))
        // Retire the legacy owner after the import commits. Fresh Room data wins via INSERT IGNORE.
        legacy.accountInfo.delete()
        legacy.apCache.delete(); legacy.laborCache.delete(); legacy.trainCache.delete()
        legacy.recruitCache.delete(); legacy.refreshCache.delete(); legacy.meetCache.delete()
    }

    suspend fun delete(game: String, server: String, uid: String) = migrationLock.withLock {
        dao.delete(game, server, uid)
        if (game == "arknights" && legacy.accountInfo.get().uid == uid) legacy.accountInfo.delete()
        if (game == "endfield" && legacy.endfieldStamina.get().uid == uid) legacy.endfieldStamina.delete()
    }

    suspend fun writeEndfield(account: AccountEf, value: EndfieldStaminaCache) {
        dao.putIfNewer(endfieldRow(account, value))
    }

    private fun endfieldRow(account: AccountEf, value: EndfieldStaminaCache) = RealtimeCacheEntity(
        "endfield", account.serverId, account.uid, System.currentTimeMillis() / 1000, value.sampledAt,
        payload = JSONObject().put("current", value.current).put("maximum", value.maximum)
            .put("fullAt", value.fullAt).toString(),
    )

    suspend fun readEndfield(account: AccountEf): EndfieldStaminaCache {
        migrationLock.withLock {
            val old = legacy.endfieldStamina.get()
            if (old.isValid && old.uid == account.uid) {
                dao.importLegacy(endfieldRow(account, old))
                legacy.endfieldStamina.delete()
            }
        }
        val row = dao.get("endfield", account.serverId, account.uid) ?: return EndfieldStaminaCache()
        if (row.schemaVersion != 1) return EndfieldStaminaCache()
        return runCatching {
            val json = JSONObject(row.payload)
            EndfieldStaminaCache(account.uid, row.dataTimestamp, json.getLong("current"),
                json.getLong("maximum"), json.getLong("fullAt"))
        }.getOrDefault(EndfieldStaminaCache())
    }
}
