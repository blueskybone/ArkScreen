package com.blueskybone.arkscreen.repository

import com.blueskybone.arkscreen.network.NetWorkTask.Companion.pullNewRecords
import com.blueskybone.arkscreen.playerinfo.GachaProcessor
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.Gacha
import com.blueskybone.arkscreen.room.dao.GachaDao

/**
 * Created by blueskybone
 * Date: 2026/1/29
 */

class GachaRepository(
    private val gachaDao: GachaDao,
    private val processor: GachaProcessor,
    private val prefManager: PrefManager
    ) {

    suspend fun getLocalRecords(uid: String) = gachaDao.getByUid(uid)

    suspend fun syncRecords(account: AccountGc): List<Gacha> {
        val listLocal = gachaDao.getByUid(account.uid)
        val lastTs = listLocal.lastOrNull()?.ts ?: 0L

        val newList = pullNewRecords(account, lastTs)
        if (newList.isNotEmpty()) {
            gachaDao.insert(newList)
        }
        return gachaDao.getByUid(account.uid)
    }


}