package com.blueskybone.arkscreen.platform.screenshot

sealed class ScreenshotError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    data object PermissionDenied : ScreenshotError("截图权限被拒绝")

    data object PermissionMissing : ScreenshotError("尚未获取截图权限")

    data object CaptureFailed : ScreenshotError("截图失败")

    data class SystemError(
        val throwable: Throwable
    ) : ScreenshotError(throwable.message, throwable)
}