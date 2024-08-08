package com.godot17.arksc.activity;

import static com.godot17.arksc.activity.ScreenTaskActivity.getTagText;
import static com.godot17.arksc.utils.ScreenUtils.getDensityDpi;
import static com.godot17.arksc.utils.ScreenUtils.getRealHeight;
import static com.godot17.arksc.utils.ScreenUtils.getRealWidth;
import static com.godot17.arksc.utils.Utils.getAssets2CacheDir;

import static java.lang.Thread.sleep;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;

import com.godot17.arksc.App;
import com.godot17.arksc.database.NewOpe;
import com.godot17.arksc.service.DataProcessService;
import com.godot17.arksc.utils.ImageUtils;

public class ScreenshotActivity extends Activity {
    public static final String TAG = "ScreenTaskActivity";
    private MediaProjection mediaProjection;
    private static ImageReader imageReader;
    private Surface surface = null;
    private VirtualDisplay virtualDisplay;
    private int screenWidth;
    private int screenHeight;
    private int screenDensityDpi;

    private Handler mHandler;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        new Thread(() -> {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }).start();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        Log.e(TAG,"ONSTART");
        setScreenSize();
        prepareForScreenshot();
        takeOneScreenshot();
        super.onStart();
    }

    private void setScreenSize() {
        screenWidth = getRealWidth(this);
        screenHeight = getRealHeight(this);
        screenDensityDpi = getDensityDpi(this);
    }

    private void prepareForScreenshot() {
        try {
            if (imageReader == null)
                imageReader = ImageReader.newInstance(screenWidth, screenHeight,
                        PixelFormat.RGBA_8888, 2);
            surface = imageReader.getSurface();
            mediaProjection = App.createMediaProjection();
            startVirtualDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startVirtualDisplay() {
        if (virtualDisplay != null) virtualDisplay.release();
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenShot",
                screenWidth,
                screenHeight,
                screenDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
                surface,
                null,
                null
        );
    }

    private void takeOneScreenshot() {
        Thread thread = new Thread(() -> {
            try {
                sleep(300);
                Image image = imageReader.acquireLatestImage();
                Bitmap bitmap;
                String[] data;

                bitmap = ImageUtils.image_2_bitmap(image, Bitmap.Config.ARGB_8888);
                bitmap = ImageUtils.bitmap_2_target_bitmap(bitmap, screenWidth, screenHeight);
                bitmap.recycle();
                int scale = ImageUtils.getScale(screenWidth);
                String dataPath = getAssets2CacheDir(this, "target_std.dat");
                data = getTagText(bitmap, dataPath, scale).split(",");
                switch (data[0]) {
                    case "NONE":
                        throw new Exception("未获取有效信息");
                    case "WRONG":
                        throw new Exception(data[1]);
                    case "RECRUIT": {
                        Log.e(TAG, "RECRUIT");
                        //TODO:
                        Intent dataProcess = new Intent(this, DataProcessService.class);
                        dataProcess.putExtra("TAG_SORT", "RECRUIT");
                        dataProcess.putExtra("TAG_TEXT", data[1]);
                        this.startService(dataProcess);
                        break;
                    }
                }
                finish();
            } catch (Exception e) {
                mHandler.post(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        stop();
        super.onDestroy();
    }

    private void stop() {
        stopVirtualDisplay();
        stopSurface();
        imageReader = null;
        stopMediaProjection();
        releaseMutex();
    }

    private void stopMediaProjection() {
        if (mediaProjection != null) mediaProjection.stop();
        mediaProjection = null;
    }

    private void stopVirtualDisplay() {
        if (virtualDisplay != null) virtualDisplay.release();
        virtualDisplay = null;
    }

    private void stopSurface() {
        if (surface != null) surface.release();
        surface = null;
    }

    private void releaseMutex() {
        App.releaseMutexActivity();
    }
}
