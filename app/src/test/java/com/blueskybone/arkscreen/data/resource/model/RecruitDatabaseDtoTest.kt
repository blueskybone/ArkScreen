package com.blueskybone.arkscreen.data.resource.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RecruitDatabaseDtoTest {
    @Test
    fun `asset deserializes through data dto into domain model`() {
        val asset = File("src/main/assets/recruit_db.json")
        val dto = ObjectMapper().registerKotlinModule()
            .readValue(asset, RecruitDatabaseDto::class.java)
        val domain = dto.toDomain()

        assertTrue(domain.operatorList.isNotEmpty())
        assertTrue(domain.update.version.isNotBlank())
    }
}
