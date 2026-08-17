package com.blueskybone.arkscreen.ui.character

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.network.avatarUrl
import com.blueskybone.arkscreen.data.network.equipUrl
import com.blueskybone.arkscreen.data.network.skillUrl
import com.blueskybone.arkscreen.databinding.IconEquipBinding
import com.blueskybone.arkscreen.databinding.IconSkillBinding
import com.blueskybone.arkscreen.databinding.IconSkillLargeBinding
import com.blueskybone.arkscreen.domain.model.operator.Operator
import java.net.URLEncoder

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
    2 to R.drawable.evolve_phase_2,
)

private val specialIconMap = mapOf(
    1 to R.drawable.special_1,
    2 to R.drawable.special_2,
    3 to R.drawable.special_3,
)

val potentialIconMap = mapOf(
    0 to R.drawable.potential_rank_0,
    1 to R.drawable.potential_rank_1,
    2 to R.drawable.potential_rank_2,
    3 to R.drawable.potential_rank_3,
    4 to R.drawable.potential_rank_4,
    5 to R.drawable.potential_rank_5,
)

val rarityColorMap = mapOf(
    1 to R.color.rare_1,
    2 to R.color.rare_2,
    3 to R.color.rare_3,
    4 to R.color.rare_4,
    5 to R.color.rare_5,
    6 to R.color.rare_6,
)

val raritySurfaceColorMap = mapOf(
    1 to R.color.rare_1_surface,
    2 to R.color.rare_2_surface,
    3 to R.color.rare_3_surface,
    4 to R.color.rare_4_surface,
    5 to R.color.rare_5_surface,
    6 to R.color.rare_6_surface,
)

fun bindSkillView(context: Context, view: IconSkillBinding, skill: Operator.Skill, rank: Int) {
    view.root.visibility = View.VISIBLE
    view.Icon.alpha = 1.0F
    view.Icon.load("$skillUrl${skill.id}.png") {
        crossfade(true)
        crossfade(300)
    }
    bindSkillRank(context, view.MainRank, view.Special, skill.specializeLevel, rank)
}

fun resetSkillView(view: IconSkillBinding) {
    view.root.visibility = View.GONE
    view.Icon.setImageResource(R.drawable.skill_icon_default)
    view.Icon.alpha = 0.0F
    view.Special.visibility = View.GONE
    view.MainRank.visibility = View.GONE
}

fun resetSkillView(view: IconSkillLargeBinding) {
    view.root.visibility = View.GONE
    view.Icon.setImageResource(R.drawable.skill_icon_default)
    view.Icon.alpha = 0.0F
    view.Special.visibility = View.GONE
    view.MainRank.visibility = View.GONE
}

fun bindSkillViewLarge(
    context: Context,
    view: IconSkillLargeBinding,
    skill: Operator.Skill,
    rank: Int,
) {
    view.root.visibility = View.VISIBLE
    view.Icon.alpha = 1.0F
    view.Icon.load("$skillUrl${skill.id}.png") {
        crossfade(true)
        crossfade(300)
    }
    bindSkillRank(context, view.MainRank, view.Special, skill.specializeLevel, rank)
}

private fun bindSkillRank(
    context: Context,
    rankView: android.widget.TextView,
    specializeView: ImageView,
    specializeLevel: Int,
    rank: Int,
) {
    if (specializeLevel == 0) {
        rankView.text = rank.toString()
        rankView.visibility = View.VISIBLE
        specializeView.visibility = View.GONE
        return
    }

    val drawableId = specialIconMap[specializeLevel]
    if (drawableId == null) {
        rankView.text = rank.toString()
        rankView.visibility = View.VISIBLE
        specializeView.visibility = View.GONE
        return
    }
    specializeView.setImageDrawable(ContextCompat.getDrawable(context, drawableId))
    rankView.visibility = View.GONE
    specializeView.visibility = View.VISIBLE
}

fun bindEquipView(view: IconEquipBinding, equip: Operator.Equip) {
    view.root.visibility = View.VISIBLE
    view.Icon.load("$equipUrl${equip.typeIcon.uppercase()}_icon.png") {
        crossfade(true)
        crossfade(300)
    }
    if (equip.locked) {
        view.Stage.visibility = View.GONE
        view.Icon.alpha = 0.4F
    } else {
        view.Stage.text = equip.stage.toString()
        view.Stage.visibility = View.VISIBLE
        view.Icon.alpha = 1.0F
    }
}

fun resetEquipView(view: IconEquipBinding) {
    view.root.visibility = View.GONE
    view.Icon.setImageResource(R.drawable.skill_icon_default)
    view.Icon.alpha = 0.0F
    view.Stage.visibility = View.GONE
}

fun bindAvatarView(view: ImageView, skinId: String) {
    val encodedSkinId = URLEncoder.encode(skinId, "UTF-8")
    view.load("$avatarUrl$encodedSkinId.png") {
        crossfade(true)
        crossfade(300)
    }
}
