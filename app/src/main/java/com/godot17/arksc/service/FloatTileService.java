package com.godot17.arksc.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

import java.util.List;

public class FloatTileService extends android.service.quicksettings.TileService {
    private final String TAG = "FloatTileService";
    private static FloatTileService instance;
    private static boolean isRunning = false;


    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    public static FloatTileService getInstance() {
        return instance;
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile mTile = getQsTile();
        Log.e(TAG, "tile.state on click:" + mTile.getState());
        Log.e(TAG, "isRunning on click: " + isRunning);
        if (mTile.getState() == Tile.STATE_INACTIVE && !isRunning) {
            mTile.setState(Tile.STATE_ACTIVE);
            mTile.updateTile();
            isRunning = true;
            Intent noDisplayIntent = new Intent(this, NoDisplayActivity.class);
            noDisplayIntent.putExtra("START_MODE", "FLOAT_TILE");
            noDisplayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivityAndCollapse(noDisplayIntent);
            EasyWindow.cancelAll();
            EasyWindow.with(getApplication())
                    .setContentView(R.layout.window_icon)
                    .setDraggable(new SpringDraggable(SpringDraggable.ORIENTATION_HORIZONTAL))
                    .setOnClickListener(android.R.id.icon, (EasyWindow.OnClickListener<ImageView>) (window, view) -> startScreenTask()).show();
        } else {
            mTile.setState(Tile.STATE_INACTIVE);
            mTile.updateTile();
            EasyWindow.cancelAll();
            isRunning = false;
            recycleResource();
            exitService();
            System.exit(0);
            stopSelf();
        }
    }

    private void exitService() {
        DataProcessService dataProcessService = DataProcessService.getInstance();
        if (dataProcessService != null) {
            dataProcessService.stopSelf();
        }
        DataQueryService dataQueryService = DataQueryService.getInstance();
        if (dataQueryService != null) {
            dataQueryService.stopSelf();
        }
        FloatWindowService floatWindowService = FloatWindowService.getInstance();
        if (floatWindowService != null) {
            floatWindowService.stopSelf();
        }
        NotificationService notificationService = NotificationService.getInstance();
        if (notificationService != null) {
            notificationService.stopSelf();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void exitAPP() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
        for (ActivityManager.AppTask appTask : appTaskList) {
            appTask.finishAndRemoveTask();
        }
//        App instance = App.getInstance();
//        if (null != instance) {
//            instance.
//        }
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
        Tile mTile = getQsTile();
        Log.e("onstartListening", "isRunning " + isRunning);
        Log.e("onstartListening", "mTile.getState" + mTile.getState());
        if (!isRunning) {
            mTile.setState(Tile.STATE_INACTIVE);
            mTile.updateTile();
        }
    }

    @Override
    public void onStopListening() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
