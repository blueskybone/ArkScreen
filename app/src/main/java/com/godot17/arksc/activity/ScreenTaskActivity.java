package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.ScreenUtils.getDensityDpi;
import static com.godot17.arksc.utils.ScreenUtils.getRealHeight;
import static com.godot17.arksc.utils.ScreenUtils.getRealWidth;

import static com.godot17.arksc.utils.Utils.getAssets2CacheDir;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.godot17.arksc.App;
import com.godot17.arksc.BuildConfig;
import com.godot17.arksc.service.DataProcessService;
import com.godot17.arksc.service.DataQueryService;
import com.godot17.arksc.service.FloatWindowService;
import com.godot17.arksc.service.NotificationService;
import com.godot17.arksc.utils.ImageUtils;

import java.util.Arrays;

public class ScreenTaskActivity extends Activity {
    public static final String TAG = "ScreenTaskActivity";
    private NotificationService notificationService;

    private int screenWidth;
    private int screenHeight;
    private int screenDensityDpi;
    private Handler mHandler;
    private MediaProjection mediaProjection;
    private static ImageReader imageReader;
    private Surface surface = null;
    private VirtualDisplay virtualDisplay;
    private Bitmap bitmap = null;
    private String[] data;
    private boolean screenSharing = false;

    static {
        System.loadLibrary("arkjni-lib");
    }

    public static native String getTagText(Bitmap bitmap, String dataPath, int num);

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");

        screenWidth = getRealWidth(this);
        screenHeight = getRealHeight(this);
        screenDensityDpi = getDensityDpi(this);

        if (imageReader == null)
            imageReader = ImageReader.newInstance(screenWidth, screenHeight,
                    PixelFormat.RGBA_8888, 2);
        surface = imageReader.getSurface();

        new Thread(() -> {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startUsedService();
        Log.e(TAG, "onStart");
        checkScreenshotPermission();
        prepareForScreenshot();
        takeOneScreenshot(300);
        //finish();
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onRestart() {
        super.onRestart();
//
//
//        if (imageReader == null)
//            imageReader = ImageReader.newInstance(screenWidth, screenHeight,
//                    PixelFormat.RGBA_8888, 2);
//        surface = imageReader.getSurface();
    }

    private void startUsedService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.startForegroundService(new Intent(this, NotificationService.class));
        }
        this.startService(new Intent(this, FloatWindowService.class));
        this.startService(new Intent(this, DataQueryService.class));
    }

    private void checkScreenshotPermission() {
        if (App.getScreenshotPermission() == null) {
            Log.e(TAG, "checkScreenshotPermission: null");
            Intent noDisplayIntent = new Intent(this, NoDisplayActivity.class);
            noDisplayIntent.putExtra("START_MODE", "RESUME");
            noDisplayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(noDisplayIntent);
            finish();
        }
    }


    private void prepareForScreenshot() {
        screenSharing = true;

        notificationService = NotificationService.getInstance();
        if (notificationService != null) {
            notificationService.foreground();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent notificationIntent = new Intent(this, NotificationService.class);
            this.startForegroundService(notificationIntent);
        }
        try {
            mediaProjection = App.createMediaProjection();
        } catch (Exception e) {
            Log.e(TAG, "prepareForScreenSharing(): SecurityException" + e);
            mediaProjection = null;
            finish();
            return;
        }
        if (surface == null) {
            if (BuildConfig.DEBUG) Log.e(TAG, "prepareForScreenSharing(): surface == null");
            finish();
            return;
        }
        if (mediaProjection == null) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "prepareForScreenSharing(): mediaProjection == null");
            finish();
            return;
        }
        try {
            startVirtualDisplay();
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.v(TAG, "prepareForScreenSharing(): mediaProjection == null");
            finish();
        }
    }

    private void takeOneScreenshot(int delay) {

        Log.e(TAG, "takeOneScreenshot");

        mHandler.postDelayed(() -> {
            try (Image image = imageReader.acquireLatestImage()) {
                if (image != null) {
                    bitmap = ImageUtils.image_2_bitmap(image, Bitmap.Config.ARGB_8888);
                    bitmap = ImageUtils.bitmap_2_target_bitmap(bitmap, screenWidth, screenHeight);
                } else {
                    Toast.makeText(this, "imageReader, null", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "takeOneScreenshot: imageReader" + e);
            }
            if (bitmap == null) {
                Toast.makeText(this, "bitmap, null", Toast.LENGTH_SHORT).show();
                return;
            }
            int scale = ImageUtils.getScale(screenWidth);
            String dataPath = getAssets2CacheDir(this, "target_std.dat");
            data = getTagText(bitmap, dataPath, scale).split(",");
            Log.e(TAG, Arrays.toString(data));
            switch (data[0]) {
                case "NONE":
                    Toast.makeText(this, "未获取有效信息", Toast.LENGTH_SHORT).show();
                    break;
                case "WRONG":
                    Toast.makeText(this, data[1], Toast.LENGTH_SHORT).show();
                    break;
                case "RECRUIT": {
                    Log.e(TAG, "RECRUIT");
                    Intent dataProcess = new Intent(this, DataProcessService.class);
                    dataProcess.putExtra("TAG_SORT", "RECRUIT");
                    dataProcess.putExtra("TAG_TEXT", data[1]);
                    this.startService(dataProcess);
                    break;
                }
            }
            finish();
        }, delay);
    }

    private void startVirtualDisplay() {
        virtualDisplay = createVirtualDisplay();
    }

    private VirtualDisplay createVirtualDisplay() {
        mediaProjection.registerCallback(
                new ScreenTaskActivity.MediaProjectionStopCallback(), null);
        if (virtualDisplay == null) {
            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ScreenShot",
                    screenWidth, screenHeight, screenDensityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                            | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    surface, null, mHandler);
        }
        return virtualDisplay;
    }

    @Override
    protected void onDestroy() {
        Log.e("ScreenTask", "onDestroy");
        super.onDestroy();
        stopImageReader();
    }


    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("MediaProject", "onStop");
            if (virtualDisplay != null) {
                virtualDisplay.release();
            }
            if (imageReader != null) {
                imageReader.setOnImageAvailableListener(null, null);
            }
            screenSharing = false;
            mediaProjection.unregisterCallback(ScreenTaskActivity.MediaProjectionStopCallback.this);
        }
    }

    private void stopImageReader() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (imageReader != null) {
            imageReader.setOnImageAvailableListener(null, null);
            imageReader = null;
        }

    }
}
