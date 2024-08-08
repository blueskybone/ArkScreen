package com.godot17.arksc.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.godot17.arksc.App;
import com.godot17.arksc.R;
import com.godot17.arksc.activity.HomeActivity;
import com.godot17.arksc.activity.ScreenshotActivity;

public class ScreenTaskService extends Service {

    private final int FOREGROUND_SERVICE_ID = 7594;
    private final String CHANNEL_FORE_ID = "1094";
    private final String CHANNEL_FORE_NAME = "screenshot_fore_service";
    private Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        createNotification();
        super.onCreate();
    }

    private void foreground() {
        startForeground(FOREGROUND_SERVICE_ID, notification);
    }

    @Override
    public void onDestroy() {
        App.releaseMutexService();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        foreground();
        Log.e("screenTaskService","foreground");
        Intent scIntent = new Intent(this, ScreenshotActivity.class);
        scIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(scIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotification() {
        NotificationChannel channelFore = new NotificationChannel(
                CHANNEL_FORE_ID,
                CHANNEL_FORE_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channelFore);

        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        notification = new Notification.Builder(this, CHANNEL_FORE_ID)
                .setChannelId(CHANNEL_FORE_ID)
                .setSmallIcon(R.drawable.ic_rosm)
                .setContentIntent(pendingIntent)
                .setContentTitle("正在截图...")
                .setContentText("此通知是用于截图的必要操作，可自行关闭")
                .build();
    }
}
