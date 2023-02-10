package com.example.arkscreen.Utils;


import static com.example.arkscreen.Utils.Utils.getAssetsCacheFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.example.arkscreen.service.ResultWindowService;

public class ScreenCapture {
    private static MediaProjection sMediaProjection;
    boolean isScreenCaptureStarted;
    private int mWidth;
    private int mHeight;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    private Handler mHandler;
    private final Context mContext;
    private String result_info;

    static {
        System.loadLibrary("arkjni-lib");
    }
    public static native String getTagText(Bitmap bitmap, String dataPath, int num);

    public ScreenCapture(Context context, MediaProjection mediaProjection) {
        sMediaProjection = mediaProjection;
        mContext = context;
        isScreenCaptureStarted = false;

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
    public ScreenCapture startProjection() {
        //TODO if get mediaprojection failed
//        if (sMediaProjection != null) {
//
//        } else {
//            Log.d("log_dir", "get mediaprojection failed");
//        }
        try {
            Thread.sleep(300);
            isScreenCaptureStarted = true;
        } catch (InterruptedException ignored) {
        }

        String dataPath = getAssetsCacheFile(mContext, "target_std.dat");

        WindowManager window = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display mDisplay = window.getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        mDisplay.getRealMetrics(metrics);
        int mDensity = metrics.densityDpi;
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;

        //start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(
                "ScreenShot",
                mWidth,
                mHeight,
                mDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mImageReader.getSurface(),
                null,
                mHandler);

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                if (isScreenCaptureStarted) {
                    Bitmap bitmap = null;
                    try (Image image = reader.acquireLatestImage()) {
                        if (image != null) {
                            bitmap = ImageUtils.image_2_bitmap(image, Bitmap.Config.ARGB_8888);
                            bitmap = ImageUtils.bitmap_2_target_bitmap(bitmap, mWidth, mHeight);
                            // magic number for adapt tag rect.
                            int num = mWidth / 640;
                            result_info = getTagText(bitmap, dataPath, num);
                            stopProjection();
                            sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (null != bitmap) {
                            bitmap.recycle();
                        }
                        Intent intent = new Intent(mContext, ResultWindowService.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("result_data", result_info);
                        mContext.startService(intent);
                    }
                }
            }
        }, mHandler);
        return this;
    }

    public void stopProjection() {
        isScreenCaptureStarted = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (sMediaProjection != null) {
                    sMediaProjection.stop();
                }
            }
        });
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) {
                        mVirtualDisplay.release();
                    }
                    if (mImageReader != null) {
                        mImageReader.setOnImageAvailableListener(null, null);
                    }
                    sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }
}
