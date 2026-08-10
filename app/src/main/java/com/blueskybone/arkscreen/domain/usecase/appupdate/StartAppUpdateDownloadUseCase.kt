package com.blueskybone.arkscreen.domain.usecase.appupdate

import com.blueskybone.arkscreen.domain.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

class StartAppUpdateDownloadUseCase(
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase,
) {
    operator fun invoke(url: String): Flow<DownloadStatus> =
        downloadAppUpdateUseCase(url)
}
