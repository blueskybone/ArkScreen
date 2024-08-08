package com.blueskybone.arkscreen.service

import android.os.Build
import android.service.quicksettings.TileService
import android.widget.ImageView
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.task.screenshot.NoDisplayDialog
import com.hjq.window.EasyWindow

/**
 *   Created by blueskybone
 *   Date: 2024/1/29
 */


class QuickTileService : TileService() {

    override fun onClick() {
        super.onClick()
        collapseAndStartScreenTask()
    }

    private fun collapseAndStartScreenTask() {
        val dialog = NoDisplayDialog(this)
        showDialog(dialog)
        dialog.startScreenTask()
        dialog.dismiss()
    }
}


class FloatTileService : TileService() {

    companion object {
        var isRunning = false
    }

    override fun onClick() {
        super.onClick()
        collapseAndPrepareScreenTask()
        if (!isRunning) {
            EasyWindow.cancelAll()
            EasyWindow.with(application)
                .setContentView(R.layout.ic_tile)
                .setDraggable()
                //.setDraggable(SpringBackDraggable(SpringBackDraggable.ORIENTATION_HORIZONTAL))
                .setOnClickListener(
                    android.R.id.icon,
                    EasyWindow.OnClickListener { _: EasyWindow<*>?, _: ImageView? ->
                        if (Build.VERSION.SDK_INT < 34 && APP.getScreenshotPermission() != null) {
                            APP.startScreenshotRecruit()
                        } else {
                            collapseAndStartScreenTask()
                        }
                    } as EasyWindow.OnClickListener<ImageView?>)
                /*长按关闭，但是体验不好先删了*/
//                .setOnLongClickListener(
//                    android.R.id.icon,
//                    EasyWindow.OnLongClickListener { _: EasyWindow<*>?, _: ImageView? -> cancelAll() } as EasyWindow.OnLongClickListener<ImageView?>
//                )
                .show()
        } else {
            EasyWindow.cancelAll()
        }
        isRunning = !isRunning
    }

    private fun collapseAndStartScreenTask() {
        val dialog = NoDisplayDialog(this)
        showDialog(dialog)
        dialog.startScreenTask()
        dialog.dismiss()
    }

    private fun collapseAndPrepareScreenTask() {
        val dialog = NoDisplayDialog(this)
        showDialog(dialog)
        dialog.prepareScreenTask()
        dialog.dismiss()
    }
}