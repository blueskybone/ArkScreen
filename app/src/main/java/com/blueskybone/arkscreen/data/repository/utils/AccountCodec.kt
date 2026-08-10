package com.blueskybone.arkscreen.data.repository.utils

import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.domain.model.account.AccountSk

/**
 * Created by blueskybone
 * Date: 2026/4/5
 */
object AccountCodec {

    fun decodeSk(raw: String): SkCredential {
        return SkCredential(token = raw)
    }

    fun decodeEf(raw: String): EfCredential {
        return EfCredential(token = raw)
    }

    fun decodeGc(raw: String): GcCredential {
        val parts = raw.split("@")
        require(parts.size == 4) { "凭证格式错误" }
        return GcCredential(
            token = parts[0],
            akUserCenter = parts[1],
            xrToken = parts[2],
            channelMasterId = parts[3].toInt()
        )
    }

    fun encodeSk(acc: AccountSk): String {
        return acc.token
    }

    fun encodeEf(acc: AccountEf): String {
        return acc.token
    }

    fun encodeGc(acc: AccountGc): String {
        return "${acc.token}@${acc.akUserCenter}@${acc.xrToken}@${acc.channelMasterId}"
    }
}