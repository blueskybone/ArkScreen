package com.blueskybone.arkscreen.playerinfo

import com.blueskybone.arkscreen.network.model.PlayerInfoResp
import java.text.Collator

fun getOperatorData(playerInfoResp: PlayerInfoResp): List<Operator> {
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
            skills = char.skills.mapIndexed { index, skill ->
                Operator.Skill(index, skill.id, skill.specializeLevel)
            }.toMutableList() as ArrayList<Operator.Skill>

            // 模组信息
            equips = char.equip.mapIndexedNotNull { index, equip ->
                if(equip.id == "uniequip_001_kalts") println( "========TEST==========" +data.equipmentInfoMap[equip.id]!!.typeName2.toString())
                data.equipmentInfoMap[equip.id]?.takeIf { it.typeName2 != null }?.let { equipInfo ->
                    Operator.Equip(
                        index -1 ,
                        equip.id,
                        equip.locked,
                        equipInfo.typeIcon,
                        equipInfo.typeName2!!,
                        equip.level
                    )
                }
            }.toMutableList() as ArrayList<Operator.Equip>


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

            if(this.charId=="char_003_kalts"){
                println(equips.size)
                println(equips)
            }
        }
    }.sortedWith(compareOperators)
}

 val compareOperators = compareBy<Operator>(
    { -it.rarity },          // 稀有度降序
    { -it.evolvePhase },     // 精英化等级降序
    { -it.level }            // 等级降序
).thenComparator { u1, u2 ->
    // 职业按自定义顺序排序
    customOrder.indexOf(u1.profession).compareTo(customOrder.indexOf(u2.profession))
}.thenComparator { u1, u2 ->
    // 最后按中文名称排序
    collator.compare(u1.name, u2.name)
}

// 职业排序顺序
private val customOrder = listOf(
    "PIONEER", "WARRIOR", "TANK", "SNIPER",
    "CASTER", "MEDIC", "SUPPORT", "SPECIAL"
)

// 中文排序器
val collator: Collator = Collator.getInstance(java.util.Locale.CHINA)
//private fun getOpeData(playerInfoResp: PlayerInfoResp): List<Operator> {
//    var charList = ArrayList<Operator>()
//    val charsNode = playerInfoResp.data!!.chars
//    val charInfoMap = playerInfoResp.data!!.charInfoMap
//    val equipInfoMap = playerInfoResp.data!!.equipmentInfoMap
//    for (char in charsNode) {
//        val operator = Operator()
//
//        operator.charId = char.charId
//        val skin = char.skinId
////            val skinId = if ('@' in skin) {
////                skin.replace('@', '_')
////            } else if ("#1" in skin) {
////                skin.replace("#1", "")
////            } else {
////                skin.replace('#', '_')
////            }
//        operator.skinId = skin
//        operator.level = char.level
//        operator.evolvePhase = char.evolvePhase
//        operator.potentialRank = char.potentialRank
//        operator.mainSkillLvl = char.mainSkillLvl
//        operator.favorPercent = char.favorPercent
//        operator.defaultSkillId = char.defaultSkillId
//        operator.gainTime = char.gainTime
//        operator.defaultEquipId = char.defaultEquipId
//
//        for ((idx, skill) in char.skills.withIndex()) {
//            operator.skills.add(
//                Operator.Skill(
//                    idx,
//                    skill.id,
//                    skill.specializeLevel
//                )
//            )
//        }
//
//
//        var index = 0
//        for (equip in char.equip) {
//            val equipId = equip.id
//            val equipInfo = equipInfoMap[equipId]!!
//            if (equipInfo.typeName2 != null) {
//                operator.equips.add(
//                    Operator.Equip(
//                        index,
//                        equipId,
//                        equip.locked,
//                        equipInfo.typeIcon,
//                        equipInfo.typeName2.toString(),
//                        equip.level
//                    )
//                )
//                index++
//            }
//        }
//
//        val charInfo = charInfoMap[operator.charId]!!
//        operator.name = charInfo.name
//        operator.nationId = charInfo.nationId
//        operator.groupId = charInfo.groupId
//        operator.displayNumber = charInfo.displayNumber
//        operator.rarity = charInfo.rarity
//        operator.profession = charInfo.profession
//        operator.subProfessionId = charInfo.subProfessionId
////            operator.professionName = i18n.convert(operator.profession, I18n.ConvertType.Profession)
////            operator.subProfessionName =
////                i18n.convert(operator.subProfessionId, I18n.ConvertType.SubProfession)
//
//        val stringBuilder = StringBuilder()
//        for (equip in operator.equips) {
//            stringBuilder.append(equip.typeName2).append("-").append(equip.stage)
//                .append(" ")
//        }
//        operator.equipString = stringBuilder.toString()
//        charList.add(operator)
//    }
//    //排序：稀有度
//
//    charList = charList
//        .sortedWith(compareBy(collator) { it.name })
//        .sortedBy { customOrder.indexOf(it.profession) }
//        .sortedWith(customComparator).toMutableList() as ArrayList<Operator>
//    return charList
//}
//
//val customComparator = Comparator<Operator> { u1, u2 ->
//    if (u1.rarity != u2.rarity) {
//        u2.rarity - u1.rarity
//    } else {
//        if (u1.evolvePhase != u2.evolvePhase) {
//            u2.evolvePhase - u1.evolvePhase
//        } else {
//            u2.level - u1.level
//        }
//    }
//}
//
////排序：职业
//val customOrder = listOf(
//    "PIONEER", "WARRIOR", "TANK", "SNIPER",
//    "CASTER", "MEDIC", "SUPPORT", "SPECIAL"
//)
//val collator: Collator = Collator.getInstance(java.util.Locale.CHINA)
