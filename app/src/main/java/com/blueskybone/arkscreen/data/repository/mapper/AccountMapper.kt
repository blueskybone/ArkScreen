package com.blueskybone.arkscreen.data.repository.mapper

import com.blueskybone.arkscreen.data.local.room.AccountEf
import com.blueskybone.arkscreen.data.local.room.AccountGc
import com.blueskybone.arkscreen.data.local.room.AccountSk
import com.blueskybone.arkscreen.data.network.model.BasicInfoData
import com.blueskybone.arkscreen.data.network.model.BindingItem
import com.blueskybone.arkscreen.domain.model.account.AccountEf as DomainAccEf
import com.blueskybone.arkscreen.domain.model.account.AccountGc as DomainAccGc
import com.blueskybone.arkscreen.domain.model.account.AccountSk as DomainAccSk

object AccountMapper {

    fun toDomain(acc: AccountSk): DomainAccSk {
        return DomainAccSk(
            uid = acc.uid,
            nickName = acc.nickName,
            token = acc.token,
            official = acc.official,
            dId = acc.dId,
            channelMasterId = acc.channelMasterId,
        )
    }

    fun toEntity(acc: DomainAccSk): AccountSk{
        return AccountSk(
            uid = acc.uid,
            nickName = acc.nickName,
            token = acc.token,
            official = acc.official,
            dId = acc.dId,
            channelMasterId = acc.channelMasterId,
        )
    }

    fun toDomain(acc: AccountGc): DomainAccGc {
        return DomainAccGc(
            uid = acc.uid,
            nickName = acc.nickName,
            token = acc.token,
            official = acc.official,
            channelMasterId = acc.channelMasterId,
            akUserCenter = acc.akUserCenter,
            xrToken = acc.xrToken,
        )
    }

    fun toEntity(acc: DomainAccGc): AccountGc{
        return AccountGc(
            uid = acc.uid,
            nickName = acc.nickName,
            token = acc.token,
            official = acc.official,
            channelMasterId = acc.channelMasterId,
            akUserCenter = acc.akUserCenter,
            xrToken = acc.xrToken,
        )
    }

    fun toDomain(acc: AccountEf): DomainAccEf {
        return DomainAccEf(
            uid = acc.uid,
            nickName = acc.nickName,
            token = acc.token,
            official = acc.official,
            channelMasterId = acc.channelMasterId,
            dId = acc.dId,
            roleId = acc.roleId,
            serverId = acc.serverId,
        )
    }

    fun toEntity(acc: DomainAccEf): AccountEf{
        return AccountEf(
            uid = acc.uid,
            nickName = acc.nickName,
            token = acc.token,
            official = acc.official,
            channelMasterId = acc.channelMasterId,
            dId = acc.dId,
            roleId = acc.roleId,
            serverId = acc.serverId,
        )
    }


    //TODO：放这合适吗
    fun BindingItem.toSkEntities(token: String, dId: String): List<AccountSk> {
        return this.bindingList.map { user ->
            AccountSk(
                token = token, dId = dId,
                nickName = user.nickName,
                channelMasterId = user.channelMasterId,
                uid = user.uid,
                official = user.isOfficial
            )
        }
    }

    fun BindingItem.toEfEntities(token: String, dId: String): List<AccountEf> {
        return this.bindingList.flatMap { user ->
            user.roles.map { role ->
                AccountEf(
                    token = token, dId = dId,
                    nickName = role.nickname,
                    channelMasterId = user.channelMasterId,
                    uid = role.roleId, roleId = role.roleId, //uid没用，还是用roleId
                    serverId = role.serverId,
                    official = user.isOfficial
                )
            }
        }
    }

    fun BasicInfoData.toGcEntities(
        token: String,
        channelMasterId: Int,
        akUserCenter: String,
        xrToken: String
    ): AccountGc {
        return this.let { item ->
            AccountGc(
                uid = item.uid,
                nickName = item.name,
                channelMasterId = item.channelId,
                token = token,
                official = channelMasterId == 1,
                akUserCenter = akUserCenter,
                xrToken = xrToken
            )
        }
    }
}