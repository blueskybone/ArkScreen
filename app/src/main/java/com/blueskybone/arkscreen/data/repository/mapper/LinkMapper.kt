package com.blueskybone.arkscreen.data.repository.mapper

import com.blueskybone.arkscreen.data.local.room.Link
import com.blueskybone.arkscreen.domain.model.link.Link as DomainLink

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
object LinkMapper {
    fun toEntity(link: DomainLink): Link {
        return Link(
            title = link.title,
            url = link.url,
            icon = link.icon
        )
    }

    fun toDomain(link: Link): DomainLink {
        return DomainLink(
            id = link.id,
            title = link.title,
            url = link.url,
            icon = link.icon
        )
    }
}