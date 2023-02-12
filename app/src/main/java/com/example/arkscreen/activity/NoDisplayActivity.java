package com.example.arkscreen.activity;

import static com.example.arkscreen.Utils.ConfigUtils.initialProperTies;
import static com.example.arkscreen.Utils.ScreenUtils.getDensityDpi;
import static com.example.arkscreen.Utils.ScreenUtils.getRealHeight;
import static com.example.arkscreen.Utils.ScreenUtils.getRealWidth;
import static com.example.arkscreen.Utils.Utils.getAssetsCacheFile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import androidx.annotation.Nullable;

import com.example.arkscreen.Utils.ImageUtils;
import com.example.arkscreen.Utils.ScreenCapture;
import com.example.arkscreen.service.ResultWindowService;
import com.example.arkscreen.service.ScreenshotService;


public class NoDisplayActivity extends Activity {

    private Handler mHandler;
    private static final int REQUEST_CODE = 100;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    private String result_info = "initializing";


    static {
        System.loadLibrary("arkjni-lib");
    }
    public static native String getTagText(Bitmap bitmap, String dataPath, int num);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("nodisplay","nodisplay");

        super.onCreate(savedInstanceState);

        initialProperTies(this);

        Intent service = new Intent(this, ScreenshotService.class);
        startForegroundService(service);

        mProjectionManager = (MediaProjectionManager) getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);

        new Thread(() -> {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }).start();

        startScreenCapture();

        //TODO
        // check database version

    }

    @Override
    protected void onRestart() {
        Intent service = new Intent(this, ScreenshotService.class);
        startForegroundService(service);
        startScreenCapture();
        super.onRestart();
    }

    private void startScreenCapture(){
        if(mMediaProjection == null){
            NoDisplayActivity.this.startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                    REQUEST_CODE);
        }
        else {
            Log.e("have mMediaprojection","have mMediaprojection");
            setUpVirtualDisplay();
        }
    }

    @SuppressLint("WrongConstant")
    private void setUpVirtualDisplay(){
        //Image mImage =  mImageReader.acquireLatestImage();

        int mWidth = getRealWidth(this);
        int mHeight = getRealHeight(this);
        int mDensityDpi = getDensityDpi(this);
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                "ScreenShot",
                mWidth,
                mHeight,
                mDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mImageReader.getSurface(),
                null,
                mHandler);

        String dataPath = getAssetsCacheFile(NoDisplayActivity.this, "target_std.dat");

        mHandler.postDelayed(() -> {
            Bitmap bitmap = null;
            try (Image image = mImageReader.acquireLatestImage()) {
                if (image != null) {
                    bitmap = ImageUtils.image_2_bitmap(image, Bitmap.Config.ARGB_8888);
                    bitmap = ImageUtils.bitmap_2_target_bitmap(bitmap, mWidth, mHeight);
                    // magic number for adapt tag rect.
                    int num = mWidth / 640;
                    try{
                        Log.e("try get text","try get text");
                        result_info = getTagText(bitmap, dataPath, num);
                    }catch (Exception e){
                        e.printStackTrace();
                        result_info = "2,识别发生问题";
                    }
                    stopProjection();
                    mMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
                }else{
                    result_info = "3,截图发生错误";
                }
                Intent intent = new Intent(NoDisplayActivity.this, ResultWindowService.class);
                //intent.setFlags(Service.START_FLAG_REDELIVERY);
                intent.putExtra("result_data", result_info);
                NoDisplayActivity.this.startForegroundService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != bitmap) {
                    bitmap.recycle();
                }
            }
        },1000);
        mHandler.postDelayed(this::finish,1000);

    }


    public void stopProjection() {
        mHandler.post(() -> {
            if (mMediaProjection != null) {
                mMediaProjection.stop();
            }
        });
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mHandler.post(() -> {
                if (mVirtualDisplay != null) {
                    mVirtualDisplay.release();
                }
                if (mImageReader != null) {
                    mImageReader.setOnImageAvailableListener(null, null);
                }
                mMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
            });
        }
    }



    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode && REQUEST_CODE == requestCode) {
            // API>29, media projection should run in a fore service
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.e("try start service","no - scshotservice");
                mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
                setUpVirtualDisplay();
            }else {
                mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
                new ScreenCapture(this, mMediaProjection).startProjection();
            }
        }

    }
}
