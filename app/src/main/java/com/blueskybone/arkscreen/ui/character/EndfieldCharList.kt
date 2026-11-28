package com.blueskybone.arkscreen.ui.character

import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldOperatorInfo
import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldCharacter

data class EndfieldCharFilters(
    val rarity: String? = null,
    val profession: String? = null,
    val property: String? = null,
) {
    val active: Boolean get() = rarity != null || profession != null || property != null
}

/** Pure list rules shared by loading and filter changes; details keep the full character list. */
internal object EndfieldCharList {
    fun value(character: EndfieldCharacter, kind: Int): String? = when (kind) {
        0 -> EndfieldCharDisplay.stars(character.charData?.rarity?.value, character.charData?.rarity?.key)
        1 -> character.charData?.profession?.key
        else -> character.charData?.property?.key
    }?.takeIf(String::isNotBlank)

    fun value(info: EndfieldOperatorInfo, kind: Int): String = when (kind) {
        0 -> "★".repeat(info.rarity)
        1 -> info.profession
        else -> info.property
    }

    fun visibleMissing(items: List<EndfieldOperatorInfo>, filters: EndfieldCharFilters) = items.filter {
        (filters.rarity == null || value(it, 0) == filters.rarity) &&
            (filters.profession == null || value(it, 1) == filters.profession) &&
            (filters.property == null || value(it, 2) == filters.property)
    }

    fun options(characters: List<EndfieldCharacter>): List<List<String>> = (0..2).map { kind ->
        characters.mapNotNull { value(it, kind) }.distinct().sortedDescending()
    }

    fun visible(characters: List<EndfieldCharacter>, filters: EndfieldCharFilters): List<EndfieldCharacter> =
        characters.filter {
            (filters.rarity == null || value(it, 0) == filters.rarity) &&
                (filters.profession == null || value(it, 1) == filters.profession) &&
                (filters.property == null || value(it, 2) == filters.property)
        }.sortedWith(
            compareByDescending<EndfieldCharacter> { it.level ?: -1 }
                .thenByDescending { it.charData?.rarity?.key?.substringAfterLast('_')?.toIntOrNull() ?: 0 }
                .thenBy { it.charData?.name.orEmpty() }
        )
}
