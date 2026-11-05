package com.blueskybone.arkscreen.data.network.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** 角色详情首版只建模可直接展示的字段；复杂描述参数继续忽略。 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldCharacter(
    val id: String? = null,
    val charData: EndfieldCharacterData? = null,
    val level: Int? = null,
    val evolvePhase: Int? = null,
    val potentialLevel: Int? = null,
    val ownTs: String? = null,
    val userSkills: Map<String, EndfieldUserSkill>? = null,
    val weapon: EndfieldWeapon? = null,
    val bodyEquip: EndfieldEquipment? = null,
    val armEquip: EndfieldEquipment? = null,
    val firstAccessory: EndfieldEquipment? = null,
    val secondAccessory: EndfieldEquipment? = null,
    val tacticalItem: EndfieldTacticalItem? = null,
    val talent: EndfieldTalentState? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldCharacterData(
    val id: String? = null,
    val name: String? = null,
    val avatarSqUrl: String? = null,
    val avatarRtUrl: String? = null,
    val illustrationUrl: String? = null,
    val rarity: EndfieldLabel? = null,
    val profession: EndfieldLabel? = null,
    val property: EndfieldLabel? = null,
    val weaponType: EndfieldLabel? = null,
    val tags: List<String>? = null,
    val skills: List<EndfieldSkill>? = null,
    val abilityTalents: List<EndfieldTalent>? = null,
    val combatTalents: List<EndfieldTalent>? = null,
    val cultivationTalents: List<EndfieldTalent>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldSkill(
    val id: String? = null,
    val name: String? = null,
    val iconUrl: String? = null,
    val type: EndfieldLabel? = null,
    val property: EndfieldLabel? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldUserSkill(
    val skillId: String? = null,
    val level: Int? = null,
    val maxLevel: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldTalent(
    val id: String? = null,
    val name: String? = null,
    val iconUrl: String? = null,
    val lockedIconUrl: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldTalentState(
    val attrNodes: List<String>? = null,
    val latestPassiveSkillNodes: List<String>? = null,
    val latestFactorySkillNodes: List<String>? = null,
    val latestSpaceshipSkillNodes: List<String>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldWeapon(
    val level: Int? = null,
    val refineLevel: Int? = null,
    val breakthroughLevel: Int? = null,
    val weaponData: EndfieldWeaponData? = null,
    val gem: EndfieldGem? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldGem(
    val icon: String? = null,
    val gemData: EndfieldGemData? = null,
    val terms: List<EndfieldGemTerm>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldGemData(
    val name: String? = null,
    val icon: String? = null,
    val templateId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldGemTerm(
    val name: String? = null,
    val cost: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldWeaponData(
    val name: String? = null,
    val iconUrl: String? = null,
    val rarity: EndfieldLabel? = null,
    val type: EndfieldLabel? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldEquipment(
    val equipData: EndfieldEquipmentData? = null,
    val enhance: Map<String, Int>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldEquipmentData(
    val name: String? = null,
    val iconUrl: String? = null,
    val rarity: EndfieldLabel? = null,
    val level: EndfieldLabel? = null,
    val suit: EndfieldEquipmentSuit? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldEquipmentSuit(
    val name: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldTacticalItem(
    val tacticalItemData: EndfieldTacticalItemData? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldTacticalItemData(
    val name: String? = null,
    val iconUrl: String? = null,
    val rarity: EndfieldLabel? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldLabel(
    val key: String? = null,
    val value: String? = null,
)
