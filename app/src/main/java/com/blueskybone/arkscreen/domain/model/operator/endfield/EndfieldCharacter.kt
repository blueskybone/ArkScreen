package com.blueskybone.arkscreen.domain.model.operator.endfield


/** Endfield operator assets, independent of network serialization. */
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

data class EndfieldSkill(
    val id: String? = null,
    val name: String? = null,
    val iconUrl: String? = null,
    val type: EndfieldLabel? = null,
    val property: EndfieldLabel? = null,
)

data class EndfieldUserSkill(
    val skillId: String? = null,
    val level: Int? = null,
    val maxLevel: Int? = null,
)

data class EndfieldTalent(
    val id: String? = null,
    val name: String? = null,
    val iconUrl: String? = null,
    val lockedIconUrl: String? = null,
)

data class EndfieldTalentState(
    val attrNodes: List<String>? = null,
    val latestPassiveSkillNodes: List<String>? = null,
    val latestFactorySkillNodes: List<String>? = null,
    val latestSpaceshipSkillNodes: List<String>? = null,
)

data class EndfieldWeapon(
    val level: Int? = null,
    val refineLevel: Int? = null,
    val breakthroughLevel: Int? = null,
    val weaponData: EndfieldWeaponData? = null,
    val gem: EndfieldGem? = null,
)

data class EndfieldGem(
    val icon: String? = null,
    val gemData: EndfieldGemData? = null,
    val terms: List<EndfieldGemTerm>? = null,
)

data class EndfieldGemData(
    val name: String? = null,
    val icon: String? = null,
    val templateId: String? = null,
)

data class EndfieldGemTerm(
    val name: String? = null,
    val cost: Int? = null,
)

data class EndfieldWeaponData(
    val name: String? = null,
    val iconUrl: String? = null,
    val rarity: EndfieldLabel? = null,
    val type: EndfieldLabel? = null,
)

data class EndfieldEquipment(
    val equipData: EndfieldEquipmentData? = null,
    val enhance: Map<String, Int>? = null,
)

data class EndfieldEquipmentData(
    val name: String? = null,
    val iconUrl: String? = null,
    val rarity: EndfieldLabel? = null,
    val level: EndfieldLabel? = null,
    val suit: EndfieldEquipmentSuit? = null,
)

data class EndfieldEquipmentSuit(
    val name: String? = null,
)

data class EndfieldTacticalItem(
    val tacticalItemData: EndfieldTacticalItemData? = null,
)

data class EndfieldTacticalItemData(
    val name: String? = null,
    val iconUrl: String? = null,
    val rarity: EndfieldLabel? = null,
)

data class EndfieldLabel(
    val key: String? = null,
    val value: String? = null,
)

data class EndfieldCharacterAssets(val characters: List<EndfieldCharacter>, val avatarUrl: String?)
