package com.blueskybone.arkscreen.legacy

import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.repository.ResourceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Created by blueskybone
 * Date: 2026/3/9
 */


@Deprecated("无效usecase")
class ResourceUpdateUseCase(
    private val repo: ResourceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val type: ConfigType
) {
    @Deprecated("无效usecase")
    sealed class Result {
        object NoUpdate : Result()
        object Checking : Result()
        data class Downloading(val progress: Int) : Result()
        data class Success(val type: ConfigType) : Result()
        data class Error(val message: String) : Result()
    }
    @Deprecated("无效usecase")
    operator fun invoke(): Flow<Result> = flow {
        emit(Result.Checking)

        try {
            // 获取远程版本信息
            val remoteInfo = repo.fetchUpdateInfo(type)
            val remoteVersion = remoteInfo.version

            // 获取本地版本
            val localVersion = repo.getLocalVersion(type)

            // 比较版本
            if (remoteVersion <= localVersion) {
                emit(Result.NoUpdate)
                return@flow
            }

            // 下载新文件
            emit(Result.Downloading(0))

            repo.downloadConfig(type)

            emit(Result.Success(type))

        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "更新失败"))
        }
    }.flowOn(dispatcher)
}