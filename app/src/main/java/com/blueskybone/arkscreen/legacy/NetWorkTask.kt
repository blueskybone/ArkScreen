package com.blueskybone.arkscreen.legacy


/**
 *   Created by blueskybone
 *   Date: 2025/1/14
 */

/*
* 简单的对鹰角api的复杂流程进行封装，隐藏鉴权的细节，
* 外部只需要调用对应的功能函数，在外部的范围处理返回
*
* */

/*
* 全部放在use case 或者repo 里面
* 等重构结束这个文件也可以删了
* */

//@Deprecated("重构结束后删除")
//class NetWorkTask {
//    companion object {
//
//        suspend fun getSklandUserBinding(
//            token: String,
//            dId: String
//        ): Response<BindingResponse> {
//            val credAndToken = getCredCode(token, dId)
//            return getPlayerBinding(credAndToken.cred, credAndToken.token, dId)
//        }
//
//        @Throws(Exception::class)
//        suspend fun getGameInfoTask(accountSk: AccountSk): Response<PlayerInfoResp> {
//            val credAndToken = getCredCode(accountSk)
//            return RetrofitUtils.getGameInfo(
//                credAndToken,
//                accountSk.uid,
//                accountSk.dId
//            )
//        }
//
//        suspend fun sklandAttendance(accountSk: AccountSk): String {
//            val credAndToken = getCredCode(accountSk)
//            return doAttendance(
//                credAndToken.cred,
//                credAndToken.token,
//                accountSk.uid,
//                accountSk.channelMasterId,
//                accountSk.dId
//            )
//        }
//
//
//        suspend fun endfieldAttendance(accountEf: AccountEf): String {
//            val credAndToken = getCredCode(accountEf.token, accountEf.dId)
//            try {
//                doAttendanceForEndfield(
//                    credAndToken.cred,
//                    credAndToken.token,
//                    accountEf.dId,
//                    accountEf.roleId,
//                    accountEf.serverId
//                )
//                return "成功"
//            } catch (e: Exception) {
//                Timber.e(e.message)
//                return "签到失败"
//            }
//        }
//
//        @Throws(Exception::class)
//        private suspend fun getCredCode(accountSk: AccountSk): CredAndToken {
//            val dId1 = generateDId()
//            val grant = getGrantByToken(accountSk.token, dId1)
//            val dId2 = generateDId()
//            return getCredByGrant(grant, dId2)
//        }
//
//        @Throws(Exception::class)
//        private suspend fun getCredCode(token: String, dId: String): CredAndToken {
//            val dId1 = generateDId()
//            val grant = getGrantByToken(token, dId1)
//            val dId2 = generateDId()
//            return getCredByGrant(grant, dId2)
//        }
//
//        suspend fun pullNewRecords(
//            accountGc: AccountGc,
//            lastTs: Long
//        ): List<Gacha> {
//            val cateList = getGachaCate(accountGc)
//            val records = mutableListOf<Gacha>()
//            cateList.forEach { cate ->
//                var resp = getFirstPageRecords(accountGc, cate)
//                while (true) {
//                    val list = resp.data.list.map {
//                        Gacha(
//                            poolId = it.poolId,
//                            poolCate = it.poolId.toCate(),
//                            uid = accountGc.uid,
//                            ts = it.gachaTs,
//                            pool = it.poolName,
//                            charName = it.charName,
//                            charId = it.charId,
//                            rarity = it.rarity,
//                            isNew = it.isNew,
//                            pos = it.pos
//                        )
//                    }
//                    records.addAll(list)
//                    if (list.isEmpty()) break
//                    if (list.last().ts < lastTs) break
//                    if (!resp.data.hasMore) break
//                    resp = getMorePageRecords(accountGc, cate, list.last().pos, list.last().ts)
//                }
//            }
//            return records.toList()
//        }
//    }
//}