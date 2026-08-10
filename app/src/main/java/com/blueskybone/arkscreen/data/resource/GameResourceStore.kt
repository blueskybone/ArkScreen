package com.blueskybone.arkscreen.data.resource

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */

import com.blueskybone.arkscreen.data.repository.utils.safeResultSync
import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.ResourceSyncStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class GameResourceStore(
    private val fileStore: ResourceFileStore,
    private val updateChecker: ResourceUpdateChecker,
    private val dispatcher: CoroutineDispatcher,
) {

    private val cache = mutableMapOf<ConfigType, Any>()
    private val mutexMap = mutableMapOf<ConfigType, Mutex>()

    fun sync(type: ConfigType): Flow<ResourceSyncStatus> = flow {
        emit(ResourceSyncStatus.Checking(type))

        try {
            val remoteInfo = updateChecker.fetchUpdateInfo(type.xmlUrl)
            val localVersion = fileStore.getLocalVersion(type)

            if (remoteInfo.version <= localVersion) {
                emit(ResourceSyncStatus.UpToDate(type))
                return@flow
            }

            emit(ResourceSyncStatus.Downloading(type))

            fileStore.downloadConfig(
                fileName = type.fileName,
                link = remoteInfo.link,
            )

            clearCache(type)

            emit(ResourceSyncStatus.Updated(type))
        } catch (throwable: Throwable) {
            emit(
                ResourceSyncStatus.Failed(
                    type = type,
                    throwable = throwable,
                )
            )
        }
    }.flowOn(dispatcher)

    suspend fun <T : Any> load(
        type: ConfigType,
        parser: (File) -> T,
    ): Result<T> = safeResultSync {
        withContext(dispatcher) {
            val cached = cache[type]

            if (cached != null) {
                @Suppress("UNCHECKED_CAST")
                return@withContext cached as T
            }

            val mutex = getMutex(type)

            mutex.withLock {
                val cachedAgain = cache[type]

                if (cachedAgain != null) {
                    @Suppress("UNCHECKED_CAST")
                    return@withLock cachedAgain as T
                }

                val file = fileStore.getValidFile(type)
                val value = parser(file)

                cache[type] = value

                value
            }
        }
    }

    fun clearCache(type: ConfigType) {
        cache.remove(type)
    }

    fun clearAllCache() {
        cache.clear()
    }

    fun getResourceDate(type: ConfigType): String {
        return fileStore.getResourceDate(type)
    }

    private fun getMutex(type: ConfigType): Mutex {
        return mutexMap.getOrPut(type) {
            Mutex()
        }
    }
}