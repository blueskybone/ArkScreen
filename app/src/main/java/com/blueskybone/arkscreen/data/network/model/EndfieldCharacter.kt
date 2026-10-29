package com.blueskybone.arkscreen.data.network.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** 资产列表首版只保留角色识别、头像和基础养成信息。 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldCharacter(
    val id: String? = null,
    val charData: EndfieldCharacterData? = null,
    val level: Int? = null,
    val evolvePhase: Int? = null,
    val potentialLevel: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldCharacterData(
    val id: String? = null,
    val name: String? = null,
    val avatarSqUrl: String? = null,
    val avatarRtUrl: String? = null,
    val rarity: EndfieldLabel? = null,
    val profession: EndfieldLabel? = null,
    val property: EndfieldLabel? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndfieldLabel(
    val key: String? = null,
    val value: String? = null,
)
