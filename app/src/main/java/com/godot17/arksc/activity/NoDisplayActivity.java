package com.godot17.arksc.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.godot17.arksc.App;
import com.godot17.arksc.service.NotificationService;

public class NoDisplayActivity extends Activity {
    private static String TAG = "NoDisplayActivity";
    private static final int SCREENSHOT_REQUEST_CODE = 4552;
    private String START_MODE = "UNKNOWN";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        NotificationService notificationService = NotificationService.getInstance();
        if (notificationService != null) {
            notificationService.foreground();
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Intent notificationIntent = new Intent(this, NotificationService.class);
            this.startForegroundService(notificationIntent);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart(){
        START_MODE = getIntent().getStringExtra("START_MODE");
        super.onStart();
        if(App.getScreenshotPermission()!=null) {
            if(START_MODE.equals("QUICK_TILE") || START_MODE.equals("RESUME"))
                startScreenTask();
            finish();
        }
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
        App.setMediaProjectionManager(mediaProjectionManager);
        try {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), SCREENSHOT_REQUEST_CODE);
        } catch (Exception e) {
            Log.e(TAG, "failed with", e);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SCREENSHOT_REQUEST_CODE == requestCode && RESULT_OK == resultCode) {
            try {
                App.setScreenshotPermission(data);
            } catch (Exception e) {
                Log.e(TAG, "ontActivityForResult(createScreenCaptureIntent, ...) failed with", e);
            }
        } else {
            App.setScreenshotPermission(null);
            finish();
            return;
        }

        if(START_MODE.equals("QUICK_TILE") || START_MODE.equals("RESUME"))
            startScreenTask();
        finish();
    }

    private void startScreenTask() {
        Intent intent = new Intent(this, ScreenTaskActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }
}