package com.example.arkscreen.service;
import android.content.Intent;
import android.util.Log;

import com.example.arkscreen.activity.NoDisplayActivity;

public class SCTileService extends android.service.quicksettings.TileService {

    @Override
    public void onTileAdded() {
        Log.d("QS", "Tile added");
    }

    /**
     * Called when this tile begins listening for events.
     */
    @Override
    public void onStartListening() {
        Log.d("QS", "Start listening");

    }
    /**
     * Called when this tile moves out of the listening state.
     */
    @Override
    public void onStopListening() {
        Log.d("QS", "Stop Listening");
    }

    /**
     * Called when the user removes this tile from Quick Settings.
     */
    @Override
    public void onTileRemoved() {
        Log.d("QS", "Tile removed");
    }
    @Override
    public void onClick() {
        super.onClick();
        // open a new activity to collapse panel
        Log.e("onclick","onclick");
        Intent intent = new Intent(this, NoDisplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivityAndCollapse(intent);
    }
}
