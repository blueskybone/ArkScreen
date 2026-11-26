package com.blueskybone.arkscreen.platform.recruit.screenshot

import com.blueskybone.arkscreen.domain.model.recruit.AutoRecruitSession
import android.content.Context
import android.provider.Settings
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotCaptureService
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotPermissionActivity
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotSession
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotTaskFlowStore
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.hjq.toast.Toaster
import timber.log.Timber

class RecruitScreenshotStarter(
    private val screenshotSession: ScreenshotSession,
    private val taskFlowStore: ScreenshotTaskFlowStore,
    private val recruitScreenshotFlow: RecruitScreenshotFlow,
    private val settings: SettingPrefManager,
) {

    fun start(
        context: Context,
        source: ScreenshotStartSource,
        onComplete: ((Boolean) -> Unit)? = null,
        autoSession: AutoRecruitSession? = null,
    ) {
        Timber.tag("RecruitCapture").i(
            "Recruit capture requested: source=%s sessionAvailable=%s",
            source,
            screenshotSession.hasPermission(),
        )
        if (!Settings.canDrawOverlays(context)) {
            Timber.tag("RecruitCapture").w("Recruit capture rejected: overlay permission missing")
            Toaster.show(context.getString(R.string.floating_permission_denied))
            autoSession?.active = false
            onComplete?.invoke(false)
            return
        }

        taskFlowStore.set(
            RecruitScreenshotTaskFlow(
                source = source,
                autoSession = autoSession,
                onComplete = onComplete,
                recruitScreenshotFlow = recruitScreenshotFlow,
                captureDelayMillis = captureDelayMillis(source),
            )
        )
        if (!screenshotSession.hasPermission()) {
            Timber.tag("RecruitCapture").i("Request MediaProjection permission")
            context.startActivity(
                ScreenshotPermissionActivity.createIntent(context)
            )
        } else {
            Timber.tag("RecruitCapture").i("Reuse active MediaProjection session")
            ScreenshotCaptureService.start(context)
        }
    }

    private fun captureDelayMillis(source: ScreenshotStartSource): Long =
        if (source == ScreenshotStartSource.FloatingBall) {
            FLOATING_BALL_DELAY_MS
        } else {
            settings.screenShotDelay.get().toLongOrNull() ?: QUICK_TILE_DEFAULT_DELAY_MS
        }

    private companion object {
        const val FLOATING_BALL_DELAY_MS = 150L
        const val QUICK_TILE_DEFAULT_DELAY_MS = 1_000L
    }
}
