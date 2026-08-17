package com.blueskybone.arkscreen.domain.usecase.operator

import com.blueskybone.arkscreen.domain.model.operator.Operator
import java.text.Collator
import java.util.Locale

/** 已持有和未持有干员列表共用的排序规则。 */
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
