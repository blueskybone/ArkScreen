package com.godot17.arksc.service;

import android.content.Intent;
import com.godot17.arksc.activity.NoDisplayActivity;

public class QuickTileService extends android.service.quicksettings.TileService {


    @Override
    public void onClick() {
        super.onClick();
        // open a new activity to collapse panel
        Intent noIntent = new Intent(this, NoDisplayActivity.class);
        noIntent.putExtra("START_MODE","QUICK_TILE");
        noIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivityAndCollapse(noIntent);
    }
}
