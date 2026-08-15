package com.blueskybone.arkscreen.data.appupdate

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

class ApkValidator(private val context: Context) {

    fun validate(file: File, expectedVersionCode: Long) {
        require(file.isFile && file.length() > 0L) { "安装包为空或不存在" }

        val packageManager = context.packageManager
        val archive = packageManager.getPackageArchiveInfo(file.absolutePath, packageInfoFlags())
            ?: error("无法解析安装包")
        require(archive.packageName == context.packageName) { "安装包与当前应用不匹配" }

        val current = packageManager.getPackageInfo(context.packageName, packageInfoFlags())
        val archiveVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            archive.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            archive.versionCode.toLong()
        }
        val currentVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            current.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            current.versionCode.toLong()
        }
        require(archiveVersion >= expectedVersionCode && archiveVersion > currentVersion) {
            "安装包版本不正确"
        }
        require(signaturesOf(archive).contentDeepEquals(signaturesOf(current))) {
            "安装包签名校验失败"
        }
    }

    private fun packageInfoFlags(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }

    private fun signaturesOf(info: android.content.pm.PackageInfo): Array<ByteArray> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = checkNotNull(info.signingInfo) { "安装包缺少签名信息" }
            if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        }
        return signatures.map { it.toByteArray() }
            .sortedBy { it.contentHashCode() }
            .toTypedArray()
    }
}
