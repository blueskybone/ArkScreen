package com.example.arkscreen.activity;

import static com.example.arkscreen.Utils.ConfigUtils.initialProperTies;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.arkscreen.R;
import com.example.arkscreen.Utils.ConfigUtils;
import com.example.arkscreen.Utils.ScreenCapture;
import com.example.arkscreen.service.ResultWindowService;
import com.example.arkscreen.service.ScreenshotService;

import java.util.Timer;
import java.util.TimerTask;

public class NoDisplayActivity extends Activity {

    private Handler mHandler;
    private static final int REQUEST_CODE = 100;
    private MediaProjectionManager mProjectionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("nodisplay","nodisplay");

        super.onCreate(savedInstanceState);
        //TODO
        // check database version
        initialProperTies(this);
        mProjectionManager = (MediaProjectionManager) getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                REQUEST_CODE);
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();
    }
    @SuppressLint("WrongConstant")
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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
