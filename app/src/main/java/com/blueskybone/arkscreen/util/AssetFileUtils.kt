package com.blueskybone.arkscreen.util

import android.content.Context
import java.io.File

private val assetCopyLock = Any()

/**
 * 将只读 assets 文件复制到 JNI 等组件可直接访问的缓存路径。
 */
fun cacheAssetFile(context: Context, filename: String): Result<File> = runCatching {
    require(filename.isNotBlank()) { "Asset filename cannot be blank" }

    synchronized(assetCopyLock) {
        val cacheDirectory = context.externalCacheDir ?: context.cacheDir
        val target = File(cacheDirectory, filename)
        if (target.isFile && target.length() > 0L) {
            return@synchronized target
        }

        target.parentFile?.mkdirs()
        val temporary = File.createTempFile("${target.name}.", ".tmp", cacheDirectory)
        try {
            context.assets.open(filename).use { input ->
                temporary.outputStream().use(input::copyTo)
            }
            check(temporary.length() > 0L) { "Copied asset is empty: $filename" }

            if (!temporary.renameTo(target)) {
                temporary.copyTo(target, overwrite = true)
            }
            check(target.isFile && target.length() > 0L) {
                "Asset cache file was not created: $filename"
            }
            target
        } finally {
            temporary.delete()
        }
    }
}
