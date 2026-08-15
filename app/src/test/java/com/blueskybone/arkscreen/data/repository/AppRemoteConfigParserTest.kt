package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.domain.model.AppRemoteConfig
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AppRemoteConfigParserTest {
    private val parser = AppRemoteConfigParser(ObjectMapper())

    @Test
    fun `valid config replaces every default`() {
        val config = parser.parse(
            """
            {
              "schemaVersion": 1,
              "links": {
                "qqGroup": {"groupId": "123456789"},
                "recruitDemo": {"bvid": "BVabcdefghij"},
                "manual": {"cvId": "123456"}
              }
            }
            """.trimIndent()
        )

        assertEquals("123456789", config.qqGroupId)
        assertEquals("BVabcdefghij", config.recruitDemoBvid)
        assertEquals("123456", config.manualCvId)
    }

    @Test
    fun `invalid or missing fields fall back independently`() {
        val config = parser.parse(
            """
            {
              "schemaVersion": 1,
              "links": {
                "qqGroup": {"groupId": "invalid"},
                "recruitDemo": {"bvid": "BVabcdefghij"}
              }
            }
            """.trimIndent()
        )

        assertEquals(AppRemoteConfig.DEFAULT_QQ_GROUP_ID, config.qqGroupId)
        assertEquals("BVabcdefghij", config.recruitDemoBvid)
        assertEquals(AppRemoteConfig.DEFAULT_MANUAL_CV_ID, config.manualCvId)
    }

    @Test
    fun `unsupported schema is rejected`() {
        assertThrows(IllegalStateException::class.java) {
            parser.parse("""{"schemaVersion": 2, "links": {}}""")
        }
    }

    @Test
    fun `malformed JSON is rejected`() {
        assertThrows(Exception::class.java) {
            parser.parse("""{"schemaVersion":""")
        }
    }
}
