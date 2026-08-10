package com.blueskybone.arkscreen.domain.usecase

import com.blueskybone.arkscreen.domain.model.DownloadStatus
import com.blueskybone.arkscreen.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import java.net.URL

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */
class DownloadAppUseCase(private val repo: ResourceRepository) {
    // 这里可以使用 Flow 来实时返回下载进度
    // 结束后给前端返回结果，决定自动安装
    suspend operator fun invoke(url: URL): Flow<DownloadStatus> {
        return repo.downloadFile(url) // 这里的具体逻辑在 Data 层实现
    }
}