package com.blueskybone.arkscreen.platform.installer

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileNotFoundException

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class ApkInstaller(
    private val context: Context,
) {

    fun install(apkFilePath: String) {
        val apkFile = File(apkFilePath)

        if (!apkFile.exists()) {
            throw FileNotFoundException("APK not found: $apkFilePath")
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }
}