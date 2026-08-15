package com.blueskybone.arkscreen.domain.usecase.appupdate

import com.blueskybone.arkscreen.domain.model.AppUpdateInfo
import com.blueskybone.arkscreen.domain.model.AppVersion
import com.blueskybone.arkscreen.domain.model.DownloadStatus
import com.blueskybone.arkscreen.domain.repository.AppUpdateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckAppUpdateUseCaseTest {

    @Test
    fun returnsRemoteInfoWhenRemoteVersionIsNewer() = runBlocking {
        val remote = AppUpdateInfo(versionCode = 15L, version = "2.3.0")
        val useCase = CheckAppUpdateUseCase(
            appUpdateRepository = FakeRepository(Result.success(remote)),
            currentAppVersion = AppVersion(versionCode = 14, name = "2.2.4"),
        )

        assertEquals(remote, useCase().getOrThrow())
    }

    @Test
    fun returnsNullWhenVersionsAreEqual() = runBlocking {
        val useCase = CheckAppUpdateUseCase(
            appUpdateRepository = FakeRepository(
                Result.success(AppUpdateInfo(versionCode = 14L, version = "2.2.4"))
            ),
            currentAppVersion = AppVersion(versionCode = 14, name = "2.2.4"),
        )

        assertNull(useCase().getOrThrow())
    }

    @Test
    fun preservesRepositoryFailure() = runBlocking {
        val error = IllegalStateException("network failed")
        val useCase = CheckAppUpdateUseCase(
            appUpdateRepository = FakeRepository(Result.failure(error)),
            currentAppVersion = AppVersion(versionCode = 14, name = "2.2.4"),
        )

        assertEquals(error, useCase().exceptionOrNull())
    }

    private class FakeRepository(
        private val result: Result<AppUpdateInfo>,
    ) : AppUpdateRepository {
        override suspend fun checkAppUpdate(): Result<AppUpdateInfo> = result

        override fun downloadApk(
            url: String,
            expectedVersionCode: Long,
            fileName: String,
        ): Flow<DownloadStatus> =
            emptyFlow()

        override fun resumeApkDownload(): Flow<DownloadStatus>? = null
    }
}
