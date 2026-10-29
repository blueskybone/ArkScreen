package com.blueskybone.arkscreen.data.network.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EndfieldCardDetailResponseTest {
    private val mapper = ObjectMapper().registerKotlinModule()

    @Test
    fun parsesSelectedFieldsAndVariableDomainList() {
        val response = mapper.readValue<EndfieldCardDetailResponse>(
            """
            {
              "code": 0,
              "data": {"detail": {
                "currentTs": "1000",
                "dungeon": {"curStamina": "12", "maxStamina": "200", "maxTs": "2000"},
                "dailyMission": {"dailyActivation": 40, "maxDailyActivation": 100},
                "weeklyMission": {"score": 20, "total": 100},
                "bpSystem": {"curLevel": 5, "maxLevel": 60},
                "domain": [
                  {"domainId": "a", "name": "地区一", "settlements": [
                    {"id": "s1", "name": "站点一", "remainMoney": "2", "moneyMax": "10", "lastTickTime": "3000"}
                  ]},
                  {"domainId": "b", "name": "地区二", "moneyMgr": {"total": "8", "count": "3"}}
                ],
                "chars": [{"unused": true}]
              }}
            }
            """.trimIndent(),
        )

        val detail = requireNotNull(response.data?.detail)
        assertEquals("12", detail.dungeon?.curStamina)
        assertEquals(40, detail.dailyMission?.dailyActivation)
        assertEquals(20, detail.weeklyMission?.score)
        assertEquals(5, detail.bpSystem?.curLevel)
        assertEquals(2, detail.domain?.size)
        assertEquals("3000", detail.domain?.first()?.settlements?.first()?.lastTickTime)
        assertEquals("3", detail.domain?.last()?.moneyMgr?.count)
        assertNull(detail.domain?.last()?.settlements)
    }

    @Test
    fun parsesCharacterListAndIgnoresUnneededDetail() {
        val response = mapper.readValue<EndfieldCardDetailResponse>(
            """
            {"code":0,"data":{"detail":{"chars":[{
              "id":"char_1","level":70,"evolvePhase":2,"potentialLevel":1,
              "charData":{
                "id":"char_1","name":"测试角色","avatarSqUrl":"square.png",
                "rarity":{"key":"6","value":"六星"},
                "profession":{"key":"guard","value":"近卫"},"tags":["输出"],
                "skills":[{"id":"skill_1","name":"技能一","descParams":{"scale":"1.5"},
                  "descLevelParams":{"1":{"scale":"1.0"},"2":{"scale":{"value":"1.5"}}}}]
              },
              "userSkills":{"skill_1":{"skillId":"skill_1","level":2,"maxLevel":12}},
              "bodyEquip":null,
              "armEquip":{"equipId":"equip_1","enhance":{"1":3},
                "equipData":{"name":"测试装备","properties":["攻击"],"suit":null}},
              "weapon":{"level":60,"refineLevel":2,"weaponData":{"name":"测试武器",
                "skillInfos":[{"skill":{"key":"effect","value":"效果"},"maxLevel":9}]},
                "skills":[{"key":"effect","level":4}],
                "gem":{"gemData":{"name":"测试基质"},"terms":[{"id":"term_1","cost":2}]}},
              "talent":{"attrNodes":["node_1"],"latestFactorySkillNodes":[]},
              "unmodelledFutureField":{"anything":true}
            }]}}}
            """.trimIndent(),
        )

        val character = requireNotNull(response.data?.detail?.chars?.single())
        assertEquals("测试角色", character.charData?.name)
        assertEquals(70, character.level)
        assertEquals(2, character.evolvePhase)
        assertEquals(1, character.potentialLevel)
        assertEquals("square.png", character.charData?.avatarSqUrl)
        assertEquals("6", character.charData?.rarity?.key)
        assertEquals("guard", character.charData?.profession?.key)
    }
}
