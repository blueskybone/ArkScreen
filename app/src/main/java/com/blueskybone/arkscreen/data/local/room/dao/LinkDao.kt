package com.blueskybone.arkscreen.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.blueskybone.arkscreen.data.local.room.Link
import kotlinx.coroutines.flow.Flow

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */
@Dao
interface LinkDao {
    @Insert
    suspend fun insert(link: Link): Long

    @Query("SELECT * FROM Link ORDER BY id ASC")
    fun getAllFlow(): Flow<List<Link>>

    @Query("DELETE FROM Link WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE Link SET title = :title, url = :url, icon = :icon WHERE id = :id")
    suspend fun update(id: Long, title: String, url: String, icon: String)
}
