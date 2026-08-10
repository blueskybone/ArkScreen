package com.blueskybone.arkscreen.ui.recruit.screenshot

import android.content.Context
import android.provider.Settings
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotCaptureService
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotPermissionActivity
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotSession
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotTaskFlowStore
import com.hjq.toast.Toaster

class RecruitScreenshotStarter(
    private val screenshotSession: ScreenshotSession,
    private val taskFlowStore: ScreenshotTaskFlowStore,
    private val recruitScreenshotFlow: RecruitScreenshotFlow,
) {

    fun start(
        context: Context,
        source: ScreenshotStartSource
    ) {
        if (!Settings.canDrawOverlays(context)) {
            Toaster.show("未授予悬浮窗权限")
            return
        }

        taskFlowStore.set(
            RecruitScreenshotTaskFlow(
                source = source,
                recruitScreenshotFlow = recruitScreenshotFlow
            )
        )
        if (!screenshotSession.hasPermission()) {
            context.startActivity(
                ScreenshotPermissionActivity.createIntent(context)
            )
        } else {
            ScreenshotCaptureService.start(context)
        }
    }
}