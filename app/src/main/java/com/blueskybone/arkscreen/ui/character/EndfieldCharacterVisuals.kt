package com.blueskybone.arkscreen.ui.character

import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.network.model.EndfieldTalentState

internal object EndfieldCharacterVisuals {
    fun professionIcon(key: String?, value: String?): Int? {
        val label = value?.takeIf(String::isNotBlank)?.lowercase() ?: key.orEmpty().lowercase()
        return when {
            "术师" in label || "caster" in label -> R.drawable.ef_profession_caster
            "重装" in label || "tank" in label || "defender" in label -> R.drawable.ef_profession_tank
            "先锋" in label || "pioneer" in label || "vanguard" in label -> R.drawable.ef_profession_vanguard
            "近卫" in label || "guard" in label || "warrior" in label -> R.drawable.ef_profession_guard
            "辅助" in label || "support" in label -> R.drawable.ef_profession_support
            "突击" in label || "striker" in label || "assault" in label -> R.drawable.ef_profession_striker
            else -> null
        }
    }

    fun propertyIcon(key: String?, value: String?): Int? {
        val label = value?.takeIf(String::isNotBlank)?.lowercase() ?: key.orEmpty().lowercase()
        return when {
            "寒冷" in label || "cryst" in label || "cold" in label -> R.drawable.ef_property_cold
            "电磁" in label || "pulse" in label || "electric" in label -> R.drawable.ef_property_pulse
            "物理" in label || "physical" in label -> R.drawable.ef_property_physical
            "灼热" in label || "fire" in label -> R.drawable.ef_property_fire
            "自然" in label || "腐蚀" in label || "natural" in label -> R.drawable.ef_property_natural
            else -> null
        }
    }

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
