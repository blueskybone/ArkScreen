package com.blueskybone.arkscreen.data.local.room.dao

import androidx.room.*
import com.blueskybone.arkscreen.data.local.room.WidgetConfig

@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM WidgetConfig WHERE appWidgetId = :id")
    suspend fun get(id: Int): WidgetConfig?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(value: WidgetConfig)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefault(value: WidgetConfig)
    @Transaction
    suspend fun getOrCreate(id: Int): WidgetConfig {
        insertDefault(WidgetConfig(id))
        return checkNotNull(get(id))
    }
    @Transaction
    suspend fun getWithAppearance(id: Int, prefs: com.blueskybone.arkscreen.data.local.pref.WidgetTemplatePrefManager): WidgetConfig {
        val current = getOrCreate(id)
        val resolved = current.withAppearance(prefs)
        if (resolved != current) put(resolved)
        return resolved
    }
    @Query("DELETE FROM WidgetConfig WHERE appWidgetId IN (:ids)")
    suspend fun delete(ids: List<Int>)
    @Transaction
    suspend fun restore(oldIds: IntArray, newIds: IntArray) {
        val previous = oldIds.map { get(it) }
        delete(oldIds.toList())
        newIds.forEachIndexed { index, id -> put(previous.getOrNull(index)?.copy(appWidgetId = id) ?: WidgetConfig(id)) }
    }
}
