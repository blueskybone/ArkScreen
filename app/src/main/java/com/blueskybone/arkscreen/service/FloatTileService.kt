package com.blueskybone.arkscreen.service

import android.app.Dialog
import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.ImageView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.task.CapturePermission
import com.blueskybone.arkscreen.task.screenshot.AcquireCapturePermission
import com.blueskybone.arkscreen.task.screenshot.RecruitService
import com.hjq.toast.Toaster
import com.hjq.window.EasyWindow

/**
 *   Created by blueskybone
 *   Date: 2025/1/23
 */
class FloatTileService : TileService() {

    companion object {
        var isRunning = false
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        tile.updateTile()

        collapsePanel()
        if (!Settings.canDrawOverlays(this)) {
            Toaster.show("未授予悬浮窗权限")
            return
        }
        if (!isRunning) {

            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()

            EasyWindow.cancelAll()
            EasyWindow.with(application)
                .setContentView(R.layout.ic_tile)
                .setDraggable()
                //.setDraggable(SpringBackDraggable(SpringBackDraggable.ORIENTATION_HORIZONTAL))
                .setOnClickListener(
                    android.R.id.icon,
                    EasyWindow.OnClickListener { _: EasyWindow<*>?, _: ImageView? ->
                        if (CapturePermission.intent == null) {
                            val acquireIntent = Intent(this, AcquireCapturePermission::class.java)
                            acquireIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(acquireIntent)
                        } else {
                            val intent = Intent(this, RecruitService::class.java)
                            startService(intent)
                        }
                    } as EasyWindow.OnClickListener<ImageView?>)
                .show()
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
            EasyWindow.cancelAll()
            val intent = Intent(this, RecruitService::class.java)
            stopService(intent)
        }
        isRunning = !isRunning
    }

    private fun collapsePanel() {
        val dialog = Dialog(this)
        showDialog(dialog)
        dialog.dismiss()
    }
}