package com.example.arkscreen.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.arkscreen.R;
import com.example.arkscreen.Utils.ConfigUtils;
import com.example.arkscreen.Utils.ScreenCapture;
import com.example.arkscreen.service.ScreenshotService;

import java.util.Timer;
import java.util.TimerTask;

public class NoDisplayActivity extends Activity {

    private static final int REQUEST_CODE = 100;
    private MediaProjectionManager mProjectionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO
        // check database version
        mProjectionManager = (MediaProjectionManager) getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                REQUEST_CODE);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode && REQUEST_CODE == requestCode) {
            // API>29, media projection should run in a fore service
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent service = new Intent(this, ScreenshotService.class);
                service.putExtra("code", resultCode);
                service.putExtra("data", data);
                startForegroundService(service);
            }else {
                MediaProjection mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
                new ScreenCapture(this, mMediaProjection).startProjection();
            }
        }
        finish();
    }
}
