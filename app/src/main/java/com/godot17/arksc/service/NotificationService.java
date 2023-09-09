package com.godot17.arksc.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;

import com.godot17.arksc.activity.HomeActivity;
import com.godot17.arksc.R;

public class NotificationService extends Service {
    private static NotificationService instance;
    private final String TAG = "NotificationService";
    private Notification notification;
    private final int FOREGROUND_SERVICE_ID = 7593;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static NotificationService getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        createNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG,"onStartCommand");
        foreground();
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotification() {
        String CHANNEL_ONE_ID = "CHANNEL_ONE_ID";
        String CHANNEL_ONE_NAME = "CHANNEL_ONE_NAME";
        NotificationChannel notificationChannel;

        notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(notificationChannel);
        }
        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new Notification.Builder(this, CHANNEL_ONE_ID).setChannelId(CHANNEL_ONE_ID)
                .setTicker("Nature")
                .setSmallIcon(R.mipmap.ic_rosm)
                .setContentTitle("ArkSc")
                .setContentIntent(pendingIntent)
                .setContentText("此通知是用于截图的必要操作，可自行关闭")
                .build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
    }

    public void foreground() {
        Log.e(TAG, "onStartFore");
        startForeground(FOREGROUND_SERVICE_ID, notification);
    }

    public void background()  {
        Log.e(TAG, "onStopFore");
        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        Log.e(TAG, "onDestroy");
        stopSelf();
        super.onDestroy();
    }

    //desperate
    /*    public void createNotificationChannel() {
        Log.e("Notification","createNotification");
        String textTitle = getString(R.string.channel_name);
        String textContent = getString(R.string.channel_disc);
        String CHANNEL_ID = "CHANNEL_CAPTURE";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_rosm)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setTimeoutAfter(3000);
        //.setAutoCancel(true);
        // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        int notificationId = 1;
        startForeground(notificationId, notification);
    }
    * */
}
