package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.data.local.room.dao.LinkDao
import com.blueskybone.arkscreen.data.repository.mapper.LinkMapper
import com.blueskybone.arkscreen.data.common.repositoryResultOf
import com.blueskybone.arkscreen.domain.model.link.Link
import com.blueskybone.arkscreen.domain.repository.LinkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class LinkRepositoryImpl(
    private val linkDao: LinkDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LinkRepository {
    override fun observeLinks(): Flow<List<Link>> {
        return linkDao.getAllFlow().map { links ->
            links.map { link ->
                LinkMapper.toDomain(link)
            }
        }.flowOn(dispatcher)
    }

    override suspend fun insertLink(link: Link): Result<Long> = repositoryResultOf {
        linkDao.insert(LinkMapper.toEntity(link))
    }

    override suspend fun updateLink(link: Link): Result<Unit> = repositoryResultOf {
        val id = requireNotNull(link.id) { "链接缺少 ID" }
        linkDao.update(id, link.title, link.url, link.icon)
    }

    override suspend fun deleteLink(link: Link): Result<Unit> = repositoryResultOf {
        linkDao.delete(requireNotNull(link.id) { "链接缺少 ID" })
    }
}
