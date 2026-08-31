package com.blueskybone.arkscreen.data.gacha

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GachaImportDecoderTest {

    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val decoder = GachaImportDecoder(objectMapper, GachaBackupCodec(objectMapper))

    @Test
    fun decodeXiaoheihe_expandsSingleAndTenPullGroups() {
        val document = decoder.decode(
            "130667312.json",
            """
                {
                  "info":{"uid":130667312,"export_app":"小黑盒"},
                  "data":{
                    "1777606331":{"p":"承诺","c":[["休谟斯",3,0]]},
                    "1777606438":{"p":"承诺","c":[
                      ["蛇屠箱",3,0],["古米",3,0],["芙蓉",2,0],["红云",3,0],["月见夜",2,0],
                      ["芬",2,0],["杜宾",3,0],["波卜",4,0],["露托",3,0],["炎熔",2,0]
                    ]}
                  }
                }
            """.trimIndent(),
        )

        assertEquals(GachaImportFormat.XIAOHEIHE, document.format)
        assertEquals("130667312", document.sourceUid)
        assertEquals("小黑盒", document.sourceName)
        assertEquals(11, document.records.size)
        assertEquals((0..9).toList(), document.records.drop(1).map { it.position })
        assertEquals(1_777_606_331_000L, document.records.first().timestamp)
        assertNull(document.records.first().poolId)
        assertNull(document.records.first().charId)
    }

    @Test
    fun decodeOfficialCompressed_preservesDecimalTimestampWithoutDoubleRounding() {
        val document = decoder.decode(
            "1787032305644_official_130667312_gacha.json",
            """
                {
                  "1780286233.286":{
                    "p":"幽境狩人","pi":"LINKAGE_74_0_1",
                    "c":[["苏苏洛",3,0],["深律",4,1]],"pos":9
                  }
                }
            """.trimIndent(),
        )

        assertEquals(GachaImportFormat.OFFICIAL_COMPRESSED, document.format)
        assertEquals("130667312", document.sourceUid)
        assertEquals(1_780_286_233_286L, document.records.first().timestamp)
        assertEquals(listOf(0, 1), document.records.map { it.position })
        assertEquals("LINKAGE_74_0_1", document.records.first().poolId)
        assertTrue(document.records.last().isNew)
    }

    @Test
    fun decodeOfficialCompressed_acceptsLegacyRecordWithoutPoolId() {
        val document = decoder.decode(
            null,
            """{"1777606331":{"p":"承诺","c":[["休谟斯",3,0]]}}""",
        )

        assertNull(document.records.single().poolId)
        assertTrue(document.warnings.any { it.code == GachaImportWarning.Code.MISSING_POOL_ID })
    }

    @Test
    fun decodeXiaoheihe_canReadUidFromFileNameWhenMetadataOmitsIt() {
        val document = decoder.decode(
            "backup_130667312_gacha.json",
            """{"info":{"export_app":"小黑盒"},"data":{"1":{"p":"承诺","c":[["芬",2,0]]}}}""",
        )

        assertEquals("130667312", document.sourceUid)
        assertTrue(
            document.warnings.any {
                it.code == GachaImportWarning.Code.SOURCE_UID_FROM_FILE_NAME
            }
        )
    }

    @Test
    fun decode_rejectsUnknownJsonShape() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            decoder.decode("unknown.json", """{"hello":"world"}""")
        }

        assertEquals("无法识别寻访记录格式", error.message)
    }

    @Test
    fun decode_rejectsInvalidRarityAndNewFlag() {
        assertThrows(IllegalArgumentException::class.java) {
            decoder.decode(null, """{"1":{"p":"承诺","c":[["芬",6,0]]}}""")
        }
        assertThrows(IllegalArgumentException::class.java) {
            decoder.decode(null, """{"1":{"p":"承诺","c":[["芬",2,2]]}}""")
        }
    }
}
