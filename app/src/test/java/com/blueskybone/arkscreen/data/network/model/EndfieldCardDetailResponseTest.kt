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
                "base": {"name":"测试玩家","level":42,"worldLevel":3,"avatarUrl":"https://example.test/avatar.png","lastLoginTime":"1700000000"},
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
        assertEquals("测试玩家", detail.base?.name)
        assertEquals(42, detail.base?.level)
        assertEquals("1700000000", detail.base?.lastLoginTime)
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
                "skills":[{"id":"skill_1","name":"技能一","type":{"key":"skill_type_normal_skill","value":"战技"},
                  "property":{"key":"skill_property_fire","value":"灼热"},"descParams":{"scale":"1.5"},
                  "descLevelParams":{"1":{"scale":"1.0"},"2":{"scale":{"value":"1.5"}}}}]
              },
              "userSkills":{"skill_1":{"skillId":"skill_1","level":2,"maxLevel":12}},
              "bodyEquip":null,
              "armEquip":{"equipId":"equip_1","enhance":{"1":3},
                "equipData":{"name":"测试装备","level":{"key":"70","value":"70"},"properties":["攻击"],"suit":null}},
              "tacticalItem":{"tacticalItemData":{"name":"测试战术道具","iconUrl":"item.png"}},
              "weapon":{"level":60,"refineLevel":2,"weaponData":{"name":"测试武器",
                "skillInfos":[{"skill":{"key":"effect","value":"效果"},"maxLevel":9}]},
                "skills":[{"key":"effect","level":4}],
                "gem":{"icon":"icon_key","gemData":{"name":"测试基质","icon":"https://example.test/gem.png","templateId":"item_gem_rarity_5"},
                  "terms":[{"id":"term_1","name":"攻击提升","cost":2},{"id":"term_2","name":"暴击提升","cost":1}]}},
              "talent":{"attrNodes":["node_1"],"latestPassiveSkillNodes":["passive_0_2"],"latestFactorySkillNodes":[]},
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
        assertEquals("技能一", character.charData?.skills?.single()?.name)
        assertEquals("skill_property_fire", character.charData?.skills?.single()?.property?.key)
        assertEquals(2, character.userSkills?.values?.single()?.level)
        assertEquals("测试武器", character.weapon?.weaponData?.name)
        assertEquals(60, character.weapon?.level)
        assertEquals("测试基质", character.weapon?.gem?.gemData?.name)
        assertEquals("item_gem_rarity_5", character.weapon?.gem?.gemData?.templateId)
        assertEquals("攻击提升", character.weapon?.gem?.terms?.first()?.name)
        assertEquals(2, character.weapon?.gem?.terms?.first()?.cost)
        assertEquals("测试装备", character.armEquip?.equipData?.name)
        assertEquals("70", character.armEquip?.equipData?.level?.value)
        assertEquals(3, character.armEquip?.enhance?.get("1"))
        assertEquals("测试战术道具", character.tacticalItem?.tacticalItemData?.name)
        assertEquals("node_1", character.talent?.attrNodes?.single())
        assertEquals("passive_0_2", character.talent?.latestPassiveSkillNodes?.single())
    }
}
