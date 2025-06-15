package com.blueskybone.arkscreen.network


import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.createAccountSkList
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.doAttendance
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getBasicInfo
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getCredByGrant
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getGachaRecords
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getGrantByToken
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.Gacha
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2025/1/14
 */

class NetWorkTask {
    companion object {
        @Throws(Exception::class)
        suspend fun createAccountList(token: String, dId: String): List<AccountSk> {
            //TODO:跟踪流程
            Timber.i("createAccountList")
            val credAndToken = getCredCode(token, dId)
            Timber.i("getCredCode succ.")
            return createAccountSkList(
                credAndToken.cred,
                credAndToken.token,
                token,
                dId
            )
        }

        suspend fun createGachaAccount(channelMasterId: Int, token: String): AccountGc? {
            return getBasicInfo(channelMasterId, token)
        }

        @Throws(Exception::class)
        suspend fun getGameInfoConnectionTask(accountSk: AccountSk): Response<ResponseBody> {
            val credAndToken = getCredCode(accountSk)
            return RetrofitUtils.getGameInfoConnection(
                credAndToken,
                accountSk.uid
            )
        }


//        @Throws(Exception::class)
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
            Timber.i("try getCredCode")
            val grant = getGrantByToken(token)
            Timber.i("getGrantByToken succ.")
            return getCredByGrant(grant, dId)
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