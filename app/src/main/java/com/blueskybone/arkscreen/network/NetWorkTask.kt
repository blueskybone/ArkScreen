package com.blueskybone.arkscreen.network

import com.blueskybone.arkscreen.network.NetWorkUtils.Companion.getBasicInfo
import com.blueskybone.arkscreen.network.NetWorkUtils.Companion.getGachaRecords
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.Gacha
import javax.net.ssl.HttpsURLConnection

/**
 *   Created by blueskybone
 *   Date: 2025/1/14
 */

class NetWorkTask {
    companion object {
        @Throws(Exception::class)
        suspend fun createAccountList(token: String, dId: String): List<AccountSk> {
            val credAndToken = getCredCode(token, dId)
            return NetWorkUtils.createAccountSkList(
                credAndToken.cred,
                credAndToken.token,
                token,
                dId
            )
        }

        suspend fun createGachaAccount(channelMasterId: Int, token: String): AccountGc {
            return getBasicInfo(channelMasterId, token)
        }

//        fun downloadGameData(accountSk: AccountSk) {
//            val credAndToken = getCredCode(accountSk)
//            NetWorkUtils.getGameInfoConnection(
//                credAndToken,
//                accountSk.uid
//            )
//        }

        @Throws(Exception::class)
        suspend fun getGameInfoInputConnection(accountSk: AccountSk): HttpsURLConnection {
            val credAndToken = getCredCode(accountSk)
            return NetWorkUtils.getGameInfoConnection(
                credAndToken,
                accountSk.uid
            )
        }

        @Throws(Exception::class)
        suspend fun sklandAttendance(accountSk: AccountSk): String {
            val credAndToken = getCredCode(accountSk)
            return NetWorkUtils.logAttendance(
                credAndToken.cred,
                credAndToken.token,
                accountSk.uid,
                accountSk.channelMasterId
            )
        }

        @Throws(Exception::class)
        private suspend fun getCredCode(accountSk: AccountSk): NetWorkUtils.CredAndToken {
            val grant = NetWorkUtils.getGrantByToken(accountSk.token)
            return NetWorkUtils.getCredByGrant(grant, accountSk.dId)
        }

        @Throws(Exception::class)
        private suspend fun getCredCode(token: String, dId: String): NetWorkUtils.CredAndToken {
            val grant = NetWorkUtils.getGrantByToken(token)
            return NetWorkUtils.getCredByGrant(grant, dId)
        }

        suspend fun getNewRecords(
            token: String,
            channelMasterId: Int,
            uid: String,
            lastTs: Long?
        ): List<Gacha> {
            val newRecords = mutableListOf<Gacha>()
            for (page in 1..100) {
                val records =
                    getGachaRecords(page, token, channelMasterId, uid) ?: return newRecords
                for (record in records) {
                    if (record.ts == lastTs) return newRecords
                    else newRecords.add(record)
                }
            }
            return newRecords
        }
    }
}