package com.blueskybone.arkscreen.domain.repository

import com.blueskybone.arkscreen.domain.model.widget.WidgetConfig

interface WidgetConfigRepository {
    /** Resolve and snapshot legacy appearance defaults for an existing widget. */
    suspend fun load(id: Int): WidgetConfig
    suspend fun save(config: WidgetConfig)
    suspend fun delete(ids: List<Int>)
    suspend fun restore(oldIds: IntArray, newIds: IntArray)
}
