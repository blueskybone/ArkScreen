package com.godot17.arksc.service;

import android.content.Intent;

import com.godot17.arksc.activity.NoDisplayActivity;
import com.godot17.arksc.activity.NodisplayDialog;

public class QuickTileService extends android.service.quicksettings.TileService {
    @Override
    public void onClick() {
        super.onClick();
        collapseAndStartScreenTask();
    }

    private void collapseAndStartScreenTask() {
        NodisplayDialog dialog = new NodisplayDialog(this);
        this.showDialog(dialog);
        dialog.startScreenTask();
        dialog.dismiss();
    }
}


