package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.data.local.room.dao.LinkDao
import com.blueskybone.arkscreen.data.repository.mapper.LinkMapper
import com.blueskybone.arkscreen.data.repository.utils.safeResultSync
import com.blueskybone.arkscreen.domain.model.link.Link
import com.blueskybone.arkscreen.domain.repository.LinkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.URL

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

    override suspend fun resolveIcon(url: String): Result<String> = withContext(dispatcher) {
        runCatching {
            val html = URL(url).readText()
            Regex("""<link.*?rel=(["'])(?:icon|shortcut icon)\1.*?href=(["'])(.*?)\2""")
                .find(html)
                ?.groupValues
                ?.get(3)
                ?.let { iconPath ->
                    if (iconPath.startsWith("http")) iconPath
                    else URL(URL(url), iconPath).toString()
                }
                .orEmpty()
        }
    }


    override suspend fun insertLink(link: Link): Result<Unit> = safeResultSync {
        linkDao.insert(LinkMapper.toEntity(link))
    }

    override suspend fun updateLink(link: Link): Result<Unit> = safeResultSync {
        linkDao.update(link.id!!, link.title, link.url, link.icon)
    }

    override suspend fun deleteLink(link: Link): Result<Unit> = safeResultSync {
        linkDao.delete(link.id!!)
    }
}
