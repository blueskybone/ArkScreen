package com.godot17.arksc;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.Log;

import com.godot17.arksc.service.ScreenTaskService;

public class App extends Application {
    private static final String TAG = "App.java";
    private static MediaProjectionManager mediaProjectionManager = null;
    private static App instance;
    private static Intent screenshotPermission = null;
    private static MediaProjection mediaProjection = null;

    public App() {
        setInstance(this);
    }
    public static App getInstance() {
        return instance;
    }
    private static void setInstance(App app) {
        instance = app;
    }

    public static Intent getScreenshotPermission() {
        return screenshotPermission;
    }

    public static void setMediaProjectionManager(MediaProjectionManager mediaProjectionManager) {
        App.mediaProjectionManager = mediaProjectionManager;
    }

    private static boolean mutexActivity = true;
    private static boolean mutexService = true;

    public static void stopMediaProjection() {
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
    }

    public static void resetMediaProjection() {
        mediaProjection = null;
    }

    public static void setScreenshotPermission(final Intent permissionIntent) {
        screenshotPermission = permissionIntent;
    }


    public static MediaProjection createMediaProjection() {
        if (BuildConfig.DEBUG) Log.v(TAG, "createMediaProjection()");
        if (mediaProjection == null) {
            if (screenshotPermission == null) {
                if (BuildConfig.DEBUG) Log.v(TAG, "screenshotPermission null");
                return null;
            }
            if (BuildConfig.DEBUG) Log.v(TAG, "screenshotPermission !null");
            mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) screenshotPermission.clone());
        }
        return mediaProjection;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

/*
    public static void acquireScreenshotPermission(Context context) {
        NotificationService notificationService = NotificationService.getInstance();
        if (screenshotPermission != null) {
            if (null != mediaProjection) {
                mediaProjection.stop();
                mediaProjection = null;
            }

            if (notificationService != null) {
                notificationService.foreground();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent notificationIntent = new Intent(context, NotificationService.class);
                context.startForegroundService(notificationIntent);
            }
            mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) screenshotPermission.clone());

        } else {
            if (BuildConfig.DEBUG)
                Log.v(TAG, "acquireScreenshotPermission() -> openScreenshotPermissionRequester(context)");
            //openScreenshotPermissionRequester(context);
        }
    }

    public static void openScreenshotPermissionRequester(Context context) {
        final Intent intent = new Intent(context, AcquireScreenshotPermission.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AcquireScreenshotPermission.EXTRA_REQUEST_PERMISSION_SCREENSHOT, true);
        context.startActivity(intent);
    }
 */

    public static void startScreenshotRecruit(Context context){
        if (mutexActivity && mutexService) {
            mutexActivity = false;
            mutexService = false;
            Log.e(TAG,"mutex = true");
            Intent intent = new Intent(context, ScreenTaskService.class);
            context.startForegroundService(intent);
        }
    }

    public static void releaseMutexService() {
        mutexService = true;
    }

    public static void releaseMutexActivity() {
        mutexActivity = true;
    }

    public void stopScreenTaskService() {
        Intent intent = new Intent(this, ScreenTaskService.class);
        stopService(intent);
    }
}
