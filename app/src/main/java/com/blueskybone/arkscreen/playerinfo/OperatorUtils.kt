package com.blueskybone.arkscreen.playerinfo

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.IconEquipBinding
import com.blueskybone.arkscreen.databinding.IconSkillBinding
import com.blueskybone.arkscreen.network.avatarUrl
import com.blueskybone.arkscreen.network.equipUrl
import com.blueskybone.arkscreen.network.model.PlayerInfoResp
import com.blueskybone.arkscreen.network.skillUrl
import java.net.URLEncoder
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
                data.equipmentInfoMap[equip.id]?.takeIf { it.typeName2 != null }?.let { equipInfo ->
                    Operator.Equip(
                        index - 1,
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


val profIconMap = mapOf(
    "PIONEER" to R.drawable.icon_pioneer,
    "WARRIOR" to R.drawable.icon_warrior,
    "TANK" to R.drawable.icon_tank,
    "SNIPER" to R.drawable.icon_sniper,
    "CASTER" to R.drawable.icon_caster,
    "MEDIC" to R.drawable.icon_medic,
    "SUPPORT" to R.drawable.icon_support,
    "SPECIAL" to R.drawable.icon_special,
)

val evolveIconMap = mapOf(
    0 to R.drawable.evolve_phase_0,
    1 to R.drawable.evolve_phase_1,
    2 to R.drawable.evolve_phase_2
)
val specialIconMap = mapOf(
    1 to R.drawable.special_1,
    2 to R.drawable.special_2,
    3 to R.drawable.special_3
)

val potentialIconMap = mapOf(
    0 to R.drawable.potential_rank_0,
    1 to R.drawable.potential_rank_1,
    2 to R.drawable.potential_rank_2,
    3 to R.drawable.potential_rank_3,
    4 to R.drawable.potential_rank_4,
    5 to R.drawable.potential_rank_5
)

val rarityColorMap = mapOf(
    1 to R.color.rare_1,
    2 to R.color.rare_2,
    3 to R.color.rare_3,
    4 to R.color.rare_4,
    5 to R.color.rare_5,
    6 to R.color.rare_6,
)

fun bindSkillView(context: Context, view: IconSkillBinding, skill: Operator.Skill, rank: Int) {
    view.root.visibility = View.VISIBLE
    view.Icon.alpha = 1.0F

    val url = "$skillUrl${skill.id}.png"
    view.Icon.load(url) {
        crossfade(true)
        crossfade(300)
    }

    if (skill.specializeLevel == 0) {
        view.MainRank.text = rank.toString()
        view.MainRank.visibility = View.VISIBLE
        view.Special.visibility = View.GONE
    } else {
        val drawID = specialIconMap[skill.specializeLevel]
        val draw = ContextCompat.getDrawable(context, drawID!!)
        view.Special.setImageDrawable(draw)
        view.MainRank.visibility = View.GONE
        view.Special.visibility = View.VISIBLE
    }
}

fun bindEquipView(view: IconEquipBinding, equip: Operator.Equip) {
    view.root.visibility = View.VISIBLE
    val url = "$equipUrl${equip.typeIcon.uppercase()}_icon.png"
    view.Icon.load(url) {
        crossfade(true)
        crossfade(300)
    }
    if (!equip.locked) {
        view.Stage.text = equip.stage.toString()
        view.Stage.visibility = View.VISIBLE
        view.Icon.alpha = 1.0F
    } else {
        view.Stage.visibility = View.GONE
        view.Icon.alpha = 0.4F
    }
}

fun bindAvatarView(view: ImageView, skinId: String) {
    val skinUrl = URLEncoder.encode(skinId, "UTF-8")
    val url = "$avatarUrl$skinUrl.png"
    view.load(url) {
        crossfade(true)
        crossfade(300)
    }
}

