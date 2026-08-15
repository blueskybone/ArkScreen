package com.blueskybone.arkscreen.data.repository.mapper

import com.blueskybone.arkscreen.data.network.model.PlayerInfoResp
import com.blueskybone.arkscreen.domain.model.operator.Operator

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */
object OperatorMapper {
    fun toDomain(playerInfoResp: PlayerInfoResp): List<Operator>{
        val data = playerInfoResp.data ?: return emptyList()
        return data.chars.map { char ->
            Operator().apply {
                // 基础信息
                charId = char.charId
                skinId = char.skinId
                level = char.level
                evolvePhase = char.evolvePhase
                potentialRank = char.potentialRank
                mainSkillLvl = char.mainSkillLvl
                favorPercent = char.favorPercent
                defaultSkillId = char.defaultSkillId
                gainTime = char.gainTime
                defaultEquipId = char.defaultEquipId

                // 技能信息
                skills = char.skills.mapIndexedTo(ArrayList()) { index, skill ->
                    Operator.Skill(index, skill.id, skill.specializeLevel)
                }

                // 模组信息
                equips = char.equip.mapIndexedNotNullTo(ArrayList()) { index, equip ->
                    data.equipmentInfoMap[equip.id]?.let { equipInfo ->
                        val typeName = equipInfo.typeName2 ?: return@mapIndexedNotNullTo null
                        Operator.Equip(
                            index - 1,
                            equip.id,
                            equip.locked,
                            equipInfo.typeIcon,
                            typeName,
                            equip.level
                        )
                    }
                }


                data.charInfoMap[charId]?.let { charInfo ->
                    name = charInfo.name
                    nationId = charInfo.nationId
                    groupId = charInfo.groupId
                    displayNumber = charInfo.displayNumber
                    rarity = charInfo.rarity
                    profession = charInfo.profession
                    subProfessionId = charInfo.subProfessionId
                }

                equipString = equips.joinToString(" ") { "${it.typeName2}-${it.stage}" }
            }
        }
    }
}
