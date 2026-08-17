package com.blueskybone.arkscreen.platform.tile

import android.app.Dialog
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import org.koin.android.ext.android.inject


class FloatRecruitTileService : TileService() {

    private val floatingBallController: RecruitFloatingBallController by inject()

    override fun onClick() {
        super.onClick()
        collapsePanel()

        val active = floatingBallController.toggle()

        qsTile?.apply {
            state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }

    private fun collapsePanel() {
        val dialog = Dialog(this)
        showDialog(dialog)
        dialog.dismiss()
    }
}