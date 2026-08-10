package com.blueskybone.arkscreen.data.network.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BiliResponseTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `parses successful response when message field is absent`() {
        val response = mapper.readValue(
            """{"code":0,"data":{"item":[{"cover":"cover","bvid":"BV1"}]}}""",
            BiliResponse::class.java,
        )

        assertEquals("", response.msg)
        assertEquals("BV1", response.data?.item?.single()?.bvid)
    }

    @Test
    fun `accepts message alias in failed response without data`() {
        val response = mapper.readValue(
            """{"code":-400,"message":"请求错误"}""",
            BiliResponse::class.java,
        )

        assertEquals("请求错误", response.msg)
        assertNull(response.data)
    }
}
