package com.godot17.arksc.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.util.Log;

import com.godot17.arksc.App;

public class AcquireScreenshotPermission extends Activity {

    private static final int SCREENSHOT_REQUEST_CODE = 4552;
    @Override
    protected void onStart() {
        super.onStart();
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
        App.setMediaProjectionManager(mediaProjectionManager);
        try {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), SCREENSHOT_REQUEST_CODE);
        } catch (Exception e) {

            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SCREENSHOT_REQUEST_CODE == requestCode && RESULT_OK == resultCode) {
            try {
                App.setScreenshotPermission(data);
                App.startScreenshotRecruit(this);
                Log.e("Acc","startScreenshotRecruit");
            } catch (Exception e) {
//                Log.e(TAG, "ontActivityForResult(createScreenCaptureIntent, ...) failed with", e);
            }
        } else {
            App.setScreenshotPermission(null);
            finish();
            return;
        }
        finish();
    }
}
