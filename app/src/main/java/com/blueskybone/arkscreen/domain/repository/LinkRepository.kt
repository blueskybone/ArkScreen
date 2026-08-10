package com.blueskybone.arkscreen.domain.repository

import com.blueskybone.arkscreen.domain.model.link.Link
import kotlinx.coroutines.flow.Flow

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
interface LinkRepository {

    fun observeLinks(): Flow<List<Link>>

    suspend fun insertLink(link: Link): Result<Long>

    suspend fun updateLink(link: Link): Result<Unit>

    suspend fun deleteLink(link: Link): Result<Unit>
}
