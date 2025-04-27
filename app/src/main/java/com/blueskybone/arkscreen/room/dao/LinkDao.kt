package com.blueskybone.arkscreen.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.blueskybone.arkscreen.room.Link

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */
@Dao
interface LinkDao {
    @Insert
    suspend fun insert(link: Link)

    @Query("SELECT * FROM Link")
    suspend fun getAll(): List<Link>

    @Query("DELETE FROM Link WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE Link SET title = :title, url = :url WHERE id = :id")
    suspend fun update(id: Long, title: String, url: String)
}