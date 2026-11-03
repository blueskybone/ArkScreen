package com.blueskybone.arkscreen.ui.character

import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.network.model.EndfieldTalentState

internal object EndfieldCharacterVisuals {
    fun skillOrder(typeKey: String?): Int = when (typeKey) {
        "skill_type_normal_attack" -> 0
        "skill_type_normal_skill" -> 1
        "skill_type_combo_skill" -> 2
        "skill_type_ultimate_skill" -> 3
        else -> 4
    }

    fun isUltimate(typeKey: String?): Boolean = typeKey == "skill_type_ultimate_skill"

    fun propertyColor(key: String?): Int = when (key?.substringAfterLast('_')) {
        "pulse" -> R.color.ef_skill_pulse
        "natural" -> R.color.ef_skill_natural
        "fire" -> R.color.ef_skill_fire
        "cryst" -> R.color.ef_skill_cryst
        else -> R.color.ef_skill_physical
    }

    fun talentGroupKey(id: String?, name: String?): String =
        id?.substringBeforeLast('_')?.takeIf(String::isNotBlank)
            ?: name?.substringBefore('·').orEmpty()

    fun talentTier(id: String?): Int = id?.substringAfterLast('_')?.toIntOrNull() ?: 0

    fun isTalentUnlocked(id: String?, state: EndfieldTalentState?, category: String): Boolean {
        if (id.isNullOrBlank() || state == null) return false
        val active = when (category) {
            "ability" -> state.attrNodes.orEmpty()
            "combat" -> state.latestPassiveSkillNodes.orEmpty()
            else -> state.latestFactorySkillNodes.orEmpty() + state.latestSpaceshipSkillNodes.orEmpty()
        }
        if (id in active) return true
        if (category == "ability") return false
        val group = talentGroupKey(id, null)
        val tier = talentTier(id)
        return tier > 0 && active.any { talentGroupKey(it, null) == group && talentTier(it) >= tier }
    }
}
