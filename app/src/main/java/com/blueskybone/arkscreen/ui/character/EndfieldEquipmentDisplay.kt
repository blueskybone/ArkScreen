package com.blueskybone.arkscreen.ui.character

import com.blueskybone.arkscreen.R

internal object EndfieldEquipmentDisplay {
    fun refineProgress(enhance: Map<String, Int>?): Int {
        val levels = (1..3).map { enhance?.get(it.toString()) ?: 0 }
        return when {
            levels.all { it == 0 } -> 0
            levels.all { it >= 3 } -> 100
            else -> 50
        }
    }

    fun qualityColor(rarityKey: String?): Int = when (rarityKey?.substringAfterLast('_')) {
        "2" -> R.color.ef_equip_green
        "4" -> R.color.ef_equip_purple
        "5" -> R.color.ef_equip_gold
        else -> R.color.ef_equip_unknown
    }
}
