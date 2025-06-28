package com.blueskybone.arkscreen.playerinfo

/**
 *   Created by blueskybone
 *   Date: 2024/8/6
 */

data class Operator(
    var charId: String = "",
    var name: String = "",
    var nationId: String = "",      //阵营 - 国籍
    var groupId: String = "",       //阵营 - 组织
    var displayNumber: String = "",
    var rarity: Int = -1,
    var profession: String = "",
    var professionName: String = "",
    var subProfessionId: String = "",
    var subProfessionName: String = "",

    var skinId: String = "",
    var level: Int = -1,
    var evolvePhase: Int = -1,          //精英化
    var potentialRank: Int = -1,        //潜能
    var mainSkillLvl: Int = -1,         //技能等级
    var favorPercent: Int = -1,         //信赖
    var defaultSkillId: String = "",
    var gainTime: Long = -1L,           //获取时间
    var defaultEquipId: String = "",

    var skills: ArrayList<Skill> = ArrayList(),
    var equips: ArrayList<Equip> = ArrayList(),

    var equipString: String = ""
) {
    data class Skill(
        var index: Int = -1,
        var id: String = "",
        var specializeLevel: Int = 0,
    )

    data class Equip(
        var index: Int = -1,
        var id: String = "",
        var locked: Boolean = true,
        var typeIcon: String = "",
        var typeName2: String = "",
        var stage: Int = 1,
    )
}