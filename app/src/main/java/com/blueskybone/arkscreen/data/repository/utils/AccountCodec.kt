package com.blueskybone.arkscreen.data.repository.utils

import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.domain.model.account.AccountSk

/**
 * Created by blueskybone
 * Date: 2026/4/5
 */
object AccountCodec {

    /*
     * 当前账号导入格式有意保持简单。GC 凭证使用固定四段的 “@” 分隔格式，
     * 默认各字段本身不包含 “@”。如果以后需要增加字段、兼容包含分隔符的值，
     * 或长期保存跨版本数据，应先引入带版本号的结构化格式，再保留旧格式迁移。
     */
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
