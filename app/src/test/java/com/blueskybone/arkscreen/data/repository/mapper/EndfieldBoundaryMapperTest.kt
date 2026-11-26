package com.blueskybone.arkscreen.data.repository.mapper

import com.blueskybone.arkscreen.data.network.model.EndfieldCharacter
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaRecord
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaPool
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert.*
import org.junit.Test

class EndfieldBoundaryMapperTest {
    @Test fun mapsNestedCharacterDataWithoutLeakingDtos() {
        val dto = jacksonObjectMapper().readValue<EndfieldCharacter>("""
            {"id":"char_1","level":90,"futureField":true,
             "charData":{"name":"测试","rarity":{"key":"rarity_6","value":"六星"},
               "skills":[{"id":"s","property":{"key":"fire"}}],
               "abilityTalents":[{"id":"t","lockedIconUrl":"locked.png"}]},
             "userSkills":{"s":{"skillId":"s","level":7,"maxLevel":12}},
             "weapon":{"level":80,"weaponData":{"name":"武器"},
               "gem":{"gemData":{"templateId":"gem_5"},"terms":[{"name":"攻击","cost":2}]}},
             "armEquip":{"equipData":{"name":"护臂","suit":{"name":"套装"}},"enhance":{"1":3}},
             "talent":{"latestPassiveSkillNodes":["t"]},
             "tacticalItem":{"tacticalItemData":{"name":"道具"}}}
        """.trimIndent())
        val character = dto.toDomain()
        assertEquals("char_1", character.id)
        assertEquals(90, character.level)
        assertEquals("rarity_6", character.charData?.rarity?.key)
        assertEquals("fire", character.charData?.skills?.single()?.property?.key)
        assertEquals("locked.png", character.charData?.abilityTalents?.single()?.lockedIconUrl)
        assertEquals(7, character.userSkills?.get("s")?.level)
        assertEquals("gem_5", character.weapon?.gem?.gemData?.templateId)
        assertEquals(2, character.weapon?.gem?.terms?.single()?.cost)
        assertEquals("套装", character.armEquip?.equipData?.suit?.name)
        assertEquals(3, character.armEquip?.enhance?.get("1"))
        assertEquals(listOf("t"), character.talent?.latestPassiveSkillNodes)
        assertEquals("道具", character.tacticalItem?.tacticalItemData?.name)
        assertNull(character.bodyEquip)
        assertNull(EndfieldCharacter().toDomain().charData)
    }

    @Test fun gachaMappingPreservesIdentityAndPityInputs() {
        val entity = EndfieldGachaRecord("role", "weapon", "001", "rerun_pool", "name", "rerun",
            "item", "weapon", 6, "123", "draw", true, true, 3)
        val record = entity.toDomain()
        assertEquals("role", record.roleId)
        assertEquals("001", record.seqId)
        assertEquals("weapon", record.type)
        assertEquals("rerun_pool", record.poolId)
        assertEquals("rerun", record.poolType)
        assertEquals(3, record.poolVersion)
        assertEquals(6, record.rarity)
        assertTrue(record.isFree)
        assertTrue(record.isNew)
        val pool = EndfieldGachaPool("role", "weapon", "rerun_pool", "name", "a,b", 70).toDomain()
        assertEquals("a,b", pool.up6Ids)
        assertEquals(70, pool.officialCount)
    }
}
