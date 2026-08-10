package com.blueskybone.arkscreen.ui.character.layout

fun calculateGridHorizontalPadding(
    containerWidth: Int,
    cellWidth: Int,
    minPadding: Int,
): Int {
    val safeMinPadding = minPadding.coerceAtLeast(0)
    if (containerWidth <= 0 || cellWidth <= 0) return safeMinPadding

    val availableWidth = (containerWidth - safeMinPadding * 2).coerceAtLeast(cellWidth)
    val columnCount = (availableWidth / cellWidth).coerceAtLeast(1)
    return ((containerWidth - columnCount * cellWidth) / 2).coerceAtLeast(safeMinPadding)
}
