package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.data.local.pref.WidgetTemplatePrefManager
import com.blueskybone.arkscreen.data.local.room.dao.WidgetConfigDao
import com.blueskybone.arkscreen.data.repository.mapper.toDomain
import com.blueskybone.arkscreen.data.repository.mapper.toEntity
import com.blueskybone.arkscreen.domain.model.widget.WidgetConfig
import com.blueskybone.arkscreen.domain.repository.WidgetConfigRepository

class WidgetConfigRepositoryImpl(
    private val dao: WidgetConfigDao,
    private val defaults: WidgetTemplatePrefManager,
) : WidgetConfigRepository {
    override suspend fun load(id: Int) = dao.getWithAppearance(id, defaults).toDomain()
    override suspend fun save(config: WidgetConfig) = dao.put(config.toEntity())
    override suspend fun delete(ids: List<Int>) = dao.delete(ids)
    override suspend fun restore(oldIds: IntArray, newIds: IntArray) = dao.restore(oldIds, newIds)
}
