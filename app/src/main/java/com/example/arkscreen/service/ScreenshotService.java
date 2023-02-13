package com.example.arkscreen.service;

import android.app.Service;
import android.content.Intent;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.arkscreen.R;

public class ScreenshotService extends Service {
    private String CHANNEL_ID = "CHANNEL_CAPTURE";
    private int notificationId = 100;
    private Handler mHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        new Thread(() -> {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("scservice","scservice");
        createNotificationChannel();
        stopFore();
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
                .setTimeoutAfter(3000)
                .setAutoCancel(true);
        // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        startForeground(notificationId, builder.build());
    }

    private void stopFore(){
        mHandler.post(() -> {
            try {
                Thread.sleep(3000);
                stopForeground(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}
