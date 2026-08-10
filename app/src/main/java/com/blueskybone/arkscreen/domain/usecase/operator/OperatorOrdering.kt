package com.blueskybone.arkscreen.domain.usecase.operator

import com.blueskybone.arkscreen.domain.model.operator.Operator
import java.text.Collator
import java.util.Locale

/** Ordering shared by owned and missing operator lists. */
object OperatorOrdering {

    private val professionOrder = listOf(
        "PIONEER",
        "WARRIOR",
        "TANK",
        "SNIPER",
        "CASTER",
        "MEDIC",
        "SUPPORT",
        "SPECIAL",
    )

    private val chineseCollator: Collator = Collator.getInstance(Locale.CHINA)

    val default: Comparator<Operator> =
        compareByDescending<Operator> { it.rarity }
            .thenByDescending { it.evolvePhase }
            .thenByDescending { it.level }
            .thenBy { operator ->
                professionOrder.indexOf(operator.profession)
                    .takeIf { it >= 0 } ?: Int.MAX_VALUE
            }
            .thenComparator { left, right ->
                chineseCollator.compare(left.name, right.name)
            }
}
