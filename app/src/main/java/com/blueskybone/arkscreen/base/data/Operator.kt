package com.blueskybone.arkscreen.base.data

/**
 *   Created by blueskybone
 *   Date: 2024/8/6
 */
data class Operator(
    var charId: String = "",
    var name: String = "",
    var nationId: String = "",      //阵营
    var groupId: String = "",      //阵营
    var displayNumber: String = "",
    var rarity: Int = -1,
    var profession: String = "",
    var professionName: String = "",
    var subProfessionId: String = "",
    var subProfessionName: String = "",

    var skinId: String = "",
    var level: Int = -1,
    var evolvePhase: Int = -1,     //精英化
    var potentialRank: Int = -1,    //潜能
    var mainSkillLvl: Int = -1,     //技能等级
    var favorPercent: Int = -1,     //信赖
    var defaultSkillId: String = "",
    var gainTime: Long = -1L,       //获取时间
    var defaultEquipId: String = "",

    var skills: MutableList<Skill> = mutableListOf(),
    var equips: MutableList<Equip> = mutableListOf(),

    var equipString: String = ""
) {
    data class Skill(
        var idx: Int = -1,
        var specializeLevel: Int = 0,
    )

    data class Equip(
        var id: String = "",
        var typeIcon: String = "",
        var typeName2: String = "",
        var level: Int = 1,
    )
}