package com.blueskybone.arkscreen.data.resource

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class ResourceJsonReader(
    private val objectMapper: ObjectMapper,
) {

    fun readNode(file: File): JsonNode {
        return objectMapper.readTree(file)
    }

    fun <T> readEntity(
        file: File,
        clazz: Class<T>,
    ): T {
        return objectMapper.readValue(file, clazz)
    }
}