package com.blueskybone.arkscreen.network


import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.createAccountSkList
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.doAttendance
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getBasicInfo
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getCredByGrant
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getFirstPageRecords
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getGachaCate
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getGrantByToken
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getMorePageRecords
import com.blueskybone.arkscreen.network.model.PlayerInfoResp
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.Gacha
import retrofit2.Response

/**
 *   Created by blueskybone
 *   Date: 2025/1/14
 */

class NetWorkTask {
    companion object {
        @Throws(Exception::class)
        suspend fun createAccountList(token: String, dId: String): List<AccountSk> {
            val credAndToken = getCredCode(token, dId)
            return createAccountSkList(
                credAndToken.cred,
                credAndToken.token,
                token,
                dId
            )
        }

        suspend fun createGachaAccount(
            channelMasterId: Int,
            token: String,
            akUserCenter: String,
            xrToken: String
        ): AccountGc? {
            return getBasicInfo(channelMasterId, token, akUserCenter, xrToken)
        }

        @Throws(Exception::class)
        suspend fun getGameInfoConnectionTask(accountSk: AccountSk): Response<PlayerInfoResp> {
            val credAndToken = getCredCode(accountSk)
            return RetrofitUtils.getGameInfoConnection(
                credAndToken,
                accountSk.uid
            )
        }


        suspend fun sklandAttendance(accountSk: AccountSk): String {
            val credAndToken = getCredCode(accountSk)
            return doAttendance(
                credAndToken.cred,
                credAndToken.token,
                accountSk.uid,
                accountSk.channelMasterId
            )
        }

        @Throws(Exception::class)
        private suspend fun getCredCode(accountSk: AccountSk): CredAndToken {
            val grant = getGrantByToken(accountSk.token)
            return getCredByGrant(grant, accountSk.dId)
        }

        @Throws(Exception::class)
        private suspend fun getCredCode(token: String, dId: String): CredAndToken {
            val grant = getGrantByToken(token)
            return getCredByGrant(grant, dId)
        }

//        suspend fun getNewRecords(
//            token: String,
//            channelMasterId: Int,
//            uid: String,
//            lastTs: Long?
//        ): List<Gacha> {
//            val newRecords = mutableListOf<Gacha>()
//            for (page in 1..100) {
//                val records =
//                    getGachaRecords(page, token, channelMasterId, uid) ?: return newRecords
//                for (record in records) {
//                    if (record.ts == lastTs) return newRecords
//                    else newRecords.add(record)
//                }
//            }
//            return newRecords
//        }


        suspend fun pullNewRecords(
            accountGc: AccountGc,
            lastTs: Long
        ): List<Gacha> {
            val cateList = getGachaCate(accountGc)
            val records = mutableListOf<Gacha>()
            cateList.forEach { cate ->
                var resp = getFirstPageRecords(accountGc, cate)
                while (true) {
                    val list = resp.data.list.map {
                        Gacha(
                            poolId = it.poolId,
                            poolCate = it.poolId.toCate(),
                            uid = accountGc.uid,
                            ts = it.gachaTs,
                            pool = it.poolName,
                            charName = it.charName,
                            charId = it.charId,
                            rarity = it.rarity,
                            isNew = it.isNew,
                            pos = it.pos
                        )
                    }
                    records.addAll(list)
                    if (list.isEmpty()) break
                    if (list.last().ts < lastTs) break
                    if (!resp.data.hasMore) break
                    resp = getMorePageRecords(accountGc, cate, list.last().pos, list.last().ts)
                }
            }
            return records.toList()
        }

        private fun String.toCate(): String {
            if (this.startsWith("LIMITED")) return "LIMITED"
            if (this.startsWith("CLASSIC")) return "CLASSIC"
            if (this.startsWith("SINGLE") || this.startsWith("DOUBLE") || this.startsWith("SPECIAL")) return "NORMAL"
            return "UN"
        }
    }

}