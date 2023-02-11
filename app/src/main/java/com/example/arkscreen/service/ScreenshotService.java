package com.example.arkscreen.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.arkscreen.R;
import com.example.arkscreen.Utils.ScreenCapture;

import java.util.Objects;

public class ScreenshotService extends Service {
    private int mResultCode;
    private Intent mResultData;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private String CHANNEL_ID = "CHANNEL_CAPTURE";
    private  int notificationId = 100;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        mResultCode = intent.getIntExtra("code", -1);
        mResultData = intent.getParcelableExtra("data");
        mProjectionManager = (MediaProjectionManager)getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mProjectionManager.getMediaProjection(
                mResultCode, Objects.requireNonNull(mResultData));
        if(mMediaProjection!=null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new ScreenCapture(this, mMediaProjection).startProjection();
        }
        else {
        }
        stopForeground(true);
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        String textTitle = getString(R.string.channel_name);
        String textContent = getString(R.string.channel_description);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_tile)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTimeoutAfter(2000)
                .setAutoCancel(true);
        // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        startForeground(notificationId, builder.build());
    }

    @Override
    public void onDestroy() {
    stopForeground(true);
    super.onDestroy();
    }
}
