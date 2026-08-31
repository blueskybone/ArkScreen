package com.blueskybone.arkscreen.data.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GachaPoolCatalogParserTest {

    private val objectMapper = ObjectMapper()
    private val parser = GachaPoolCatalogParser(ResourceJsonReader(objectMapper))

    @Test
    fun parse_readsOptionalOfficialIdAndTimeRange() {
        val catalog = parser.parse(
            objectMapper.readTree(
                """
                    {"pools":[{
                      "id":"linkage_74","names":["幽境狩人"],"category":"LINKAGE",
                      "poolId":"LINKAGE_74_0_1","startTs":1000,"endTs":2000
                    }]}
                """.trimIndent()
            )
        )

        val entry = catalog.entries.single()
        assertEquals("linkage_74", entry.id)
        assertEquals(setOf("幽境狩人"), entry.names)
        assertEquals("LINKAGE_74_0_1", entry.poolId)
        assertEquals(1000L, entry.startTs)
        assertEquals(2000L, entry.endTs)
    }

    @Test
    fun parse_rejectsDuplicateIdAndInvalidCategory() {
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse(
                objectMapper.readTree(
                    """{"pools":[
                      {"id":"same","names":["A"],"category":"NORMAL"},
                      {"id":"same","names":["B"],"category":"NORMAL"}
                    ]}"""
                )
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse(
                objectMapper.readTree(
                    """{"pools":[{"id":"bad","names":["A"],"category":"WRONG"}]}"""
                )
            )
        }
    }

    @Test
    fun parse_rejectsReversedTimeRange() {
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse(
                objectMapper.readTree(
                    """{"pools":[{
                      "id":"bad_time","names":["A"],"category":"NORMAL",
                      "startTs":2000,"endTs":1000
                    }]}"""
                )
            )
        }
    }
}
