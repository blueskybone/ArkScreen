package com.blueskybone.arkscreen.data.gacha

import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.domain.model.gacha.Record
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GachaBackupCodecTest {

    private val codec = GachaBackupCodec(ObjectMapper().registerKotlinModule())
    private val account = AccountGc(
        uid = "123456",
        nickName = "Doctor",
        token = "",
        official = true,
        channelMasterId = 1,
        akUserCenter = "",
        xrToken = "",
    )
    private val record = Record(
        id = "1",
        pool = "标准寻访",
        poolId = "NORM_1",
        poolCate = "NORMAL",
        charName = "能天使",
        charId = "char_103_angel",
        rarity = 5,
        isNew = false,
        pos = 10,
        ts = 1_700_000_000_000,
    )

    @Test
    fun jsonRoundTrip_preservesSourceAndRecords() {
        val decoded = codec.decode(codec.encodeJson(account, listOf(record)))

        assertEquals(account.uid, decoded.sourceUid)
        assertEquals(account.nickName, decoded.sourceName)
        assertEquals(listOf(record), decoded.records)
    }

    @Test
    fun textRoundTrip_preservesSourceAndRecords() {
        val decoded = codec.decode(codec.encodeText(account, listOf(record)))

        assertEquals(account.uid, decoded.sourceUid)
        assertEquals(account.nickName, decoded.sourceName)
        assertEquals(record.copy(id = "${record.ts}_${record.pos}"), decoded.records.single())
    }

    @Test
    fun decode_rejectsInvalidBusinessFields() {
        val invalid = codec.encodeJson(account, listOf(record.copy(rarity = 9)))

        val error = assertThrows(IllegalArgumentException::class.java) { codec.decode(invalid) }

        assertEquals("第 1 条记录的稀有度无效", error.message)
    }

    @Test
    fun decode_rejectsUnsupportedJsonVersion() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            codec.decode("""{"version":2,"records":[]}""")
        }

        assertEquals("暂不支持此备份文件版本", error.message)
    }
}
