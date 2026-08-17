package com.blueskybone.arkscreen.domain.service

interface LinkMetadataResolver {
    suspend fun resolveIcon(url: String): Result<String>
}
