package com.godot17.arksc.service;

import android.content.Intent;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.util.Log;
import android.widget.ImageView;

import com.godot17.arksc.App;
import com.godot17.arksc.R;
import com.godot17.arksc.activity.NoDisplayActivity;
import com.godot17.arksc.activity.ScreenTaskActivity;
import com.hjq.window.EasyWindow;
import com.hjq.window.draggable.SpringDraggable;

public class FloatTileService extends android.service.quicksettings.TileService {
    private final String TAG = "FloatTileService";
    private static FloatTileService instance;
    private boolean isRunning = false;


    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        instance = this;
        isRunning = false;
        super.onCreate();
    }
    public static FloatTileService getInstance(){
        return instance;
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile mTile = getQsTile();
        if (mTile.getState() == Tile.STATE_INACTIVE) {
            mTile.setState(Tile.STATE_ACTIVE);
            mTile.updateTile();
            isRunning = true;
            Intent noDisplayIntent = new Intent(this, NoDisplayActivity.class);
            noDisplayIntent.putExtra("START_MODE", "FLOAT_TILE");
            noDisplayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivityAndCollapse(noDisplayIntent);

            //部分机型在application不在列表中时无法正常显示easyWindow，考虑写service替代
            EasyWindow.with(getApplication())
                    .setContentView(R.layout.window_icon)
                    .setYOffset(200)
                    .setDraggable(new SpringDraggable(SpringDraggable.ORIENTATION_HORIZONTAL))
                    .setOnClickListener(android.R.id.icon, (EasyWindow.OnClickListener<ImageView>) (window, view) -> startScreenTask()).show();
        } else {
            mTile.setState(Tile.STATE_INACTIVE);
            mTile.updateTile();
            EasyWindow.cancelAll();
            isRunning = false;
            recycleResource();
            System.exit(0);
        }
    }

    private void recycleResource() {
        App.stopMediaProjection();
    }

    private void startScreenTask() {
        Intent intent = new Intent(this, ScreenTaskActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @Override
    public void onStartListening() {
    }
    @Override
    public void onStopListening(){

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
