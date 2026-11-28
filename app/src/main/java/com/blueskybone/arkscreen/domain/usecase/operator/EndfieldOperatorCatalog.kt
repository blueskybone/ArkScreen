package com.blueskybone.arkscreen.domain.usecase.operator

import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldCharacter
import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldOperatorInfo

object EndfieldOperatorCatalog {
    fun missing(catalog: List<EndfieldOperatorInfo>, owned: List<EndfieldCharacter>): List<EndfieldOperatorInfo> {
        val ids = owned.mapNotNull { it.id }.toSet()
        return catalog.filter {
            !it.isPreview && it.wikiId !in setOf("89", "156") && it.charId != null && it.charId !in ids
        }.sortedWith(compareByDescending<EndfieldOperatorInfo> { it.rarity }.thenBy { it.name })
    }

    fun wikiEntry(catalog: List<EndfieldOperatorInfo>, character: EndfieldCharacter): EndfieldOperatorInfo? {
        // charData.id distinguishes the female administrator; top-level id does not.
        return catalog.firstOrNull { it.charId != null && it.charId == character.charData?.id }
            ?: catalog.firstOrNull { it.charId != null && it.charId == character.id }
    }
}
