package com.blueskybone.arkscreen.service

import android.app.Dialog
import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.TileService
import com.blueskybone.arkscreen.task.CapturePermission
import com.blueskybone.arkscreen.task.screenshot.AcquireCapturePermission
import com.blueskybone.arkscreen.task.screenshot.RecruitService
import com.hjq.toast.Toaster

/**
 *   Created by blueskybone
 *   Date: 2025/1/23
 */

class QuickTileService : TileService() {
    override fun onClick() {
        super.onClick()
        collapsePanel()
        if (!Settings.canDrawOverlays(this)) {
            Toaster.show("未授予悬浮窗权限")
            return
        }
        if (CapturePermission.intent == null) {
            val acquireIntent = Intent(this, AcquireCapturePermission::class.java)
            acquireIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(acquireIntent)
        } else {
            val intent = Intent(this, RecruitService::class.java)
            startService(intent)
        }
    }


    private fun collapsePanel() {
        val dialog = Dialog(this)
        showDialog(dialog)
        dialog.dismiss()
    }
}