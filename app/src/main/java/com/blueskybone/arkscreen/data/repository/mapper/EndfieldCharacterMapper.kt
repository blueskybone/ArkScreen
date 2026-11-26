package com.blueskybone.arkscreen.data.repository.mapper

import com.blueskybone.arkscreen.data.network.model.*

internal fun EndfieldCharacter.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldCharacter(
    id = id,
    charData = charData?.toDomain(),
    level = level,
    evolvePhase = evolvePhase,
    potentialLevel = potentialLevel,
    ownTs = ownTs,
    userSkills = userSkills?.mapValues { (_, value) -> value.toDomain() },
    weapon = weapon?.toDomain(),
    bodyEquip = bodyEquip?.toDomain(),
    armEquip = armEquip?.toDomain(),
    firstAccessory = firstAccessory?.toDomain(),
    secondAccessory = secondAccessory?.toDomain(),
    tacticalItem = tacticalItem?.toDomain(),
    talent = talent?.toDomain(),
)

internal fun EndfieldCharacterData.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldCharacterData(
    id = id,
    name = name,
    avatarSqUrl = avatarSqUrl,
    avatarRtUrl = avatarRtUrl,
    illustrationUrl = illustrationUrl,
    rarity = rarity?.toDomain(),
    profession = profession?.toDomain(),
    property = property?.toDomain(),
    weaponType = weaponType?.toDomain(),
    tags = tags,
    skills = skills?.map { it.toDomain() },
    abilityTalents = abilityTalents?.map { it.toDomain() },
    combatTalents = combatTalents?.map { it.toDomain() },
    cultivationTalents = cultivationTalents?.map { it.toDomain() },
)

internal fun EndfieldSkill.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldSkill(
    id = id,
    name = name,
    iconUrl = iconUrl,
    type = type?.toDomain(),
    property = property?.toDomain(),
)

internal fun EndfieldUserSkill.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldUserSkill(
    skillId = skillId,
    level = level,
    maxLevel = maxLevel,
)

internal fun EndfieldTalent.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldTalent(
    id = id,
    name = name,
    iconUrl = iconUrl,
    lockedIconUrl = lockedIconUrl,
)

internal fun EndfieldTalentState.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldTalentState(
    attrNodes = attrNodes,
    latestPassiveSkillNodes = latestPassiveSkillNodes,
    latestFactorySkillNodes = latestFactorySkillNodes,
    latestSpaceshipSkillNodes = latestSpaceshipSkillNodes,
)

internal fun EndfieldWeapon.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldWeapon(
    level = level,
    refineLevel = refineLevel,
    breakthroughLevel = breakthroughLevel,
    weaponData = weaponData?.toDomain(),
    gem = gem?.toDomain(),
)

internal fun EndfieldGem.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldGem(
    icon = icon,
    gemData = gemData?.toDomain(),
    terms = terms?.map { it.toDomain() },
)

internal fun EndfieldGemData.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldGemData(
    name = name,
    icon = icon,
    templateId = templateId,
)

internal fun EndfieldGemTerm.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldGemTerm(
    name = name,
    cost = cost,
)

internal fun EndfieldWeaponData.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldWeaponData(
    name = name,
    iconUrl = iconUrl,
    rarity = rarity?.toDomain(),
    type = type?.toDomain(),
)

internal fun EndfieldEquipment.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldEquipment(
    equipData = equipData?.toDomain(),
    enhance = enhance,
)

internal fun EndfieldEquipmentData.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldEquipmentData(
    name = name,
    iconUrl = iconUrl,
    rarity = rarity?.toDomain(),
    level = level?.toDomain(),
    suit = suit?.toDomain(),
)

internal fun EndfieldEquipmentSuit.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldEquipmentSuit(
    name = name,
)

internal fun EndfieldTacticalItem.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldTacticalItem(
    tacticalItemData = tacticalItemData?.toDomain(),
)

internal fun EndfieldTacticalItemData.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldTacticalItemData(
    name = name,
    iconUrl = iconUrl,
    rarity = rarity?.toDomain(),
)

internal fun EndfieldLabel.toDomain() = com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldLabel(
    key = key,
    value = value,
)
