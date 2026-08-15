package com.blueskybone.arkscreen.platform.tile

import android.app.Dialog
import android.service.quicksettings.TileService
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.ui.recruit.screenshot.RecruitScreenshotStarter
import org.koin.android.ext.android.inject

class QuickRecruitTileService : TileService() {

    private val starter: RecruitScreenshotStarter by inject()

    override fun onClick() {
        super.onClick()
        collapsePanel()

        starter.start(
            context = this,
            source = ScreenshotStartSource.QuickTile
        )
    }

    private fun collapsePanel() {
        val dialog = Dialog(this)
        showDialog(dialog)
        dialog.dismiss()
    }
}