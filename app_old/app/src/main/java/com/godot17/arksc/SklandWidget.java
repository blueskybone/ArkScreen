package com.godot17.arksc;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of App Widget functionality.
 */
public class SklandWidget extends AppWidgetProvider {

    private static final String TAG = "SklandWidget";
    public static final String APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String MANUAL_UPDATE = "MANUAL_UPDATE";
    private static final String WORKER_NAME = "SklandWorker";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent intent = new Intent();
        intent.setClass(context, SklandWidget.class);
        intent.setAction(MANUAL_UPDATE);

        //设置pendingIntent
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }
        //Retrieve a PendingIntent that will perform a broadcast
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.skland_widget);

        //为刷新按钮绑定一个事件便于发送广播
        remoteViews.setOnClickPendingIntent(R.id.refresh, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

        //Log.e(TAG, "OnUpdate");
        //WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(SklandWorker.class));
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(SklandWorker.class,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS * 2, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORKER_NAME, ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        WorkManager.getInstance(context).cancelUniqueWork(WORKER_NAME);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.e(TAG, "OnReceive");
        if (intent.getAction().equals(MANUAL_UPDATE)||intent.getAction().equals(APPWIDGET_UPDATE)) {
            Log.e(TAG, "receive");
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(SklandWorker.class));

//            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context.getPackageName(), SklandWidget.class.getName()));
//            for (int appWidgetId : appWidgetIds) {
//                //updateAppWidget(context, appWidgetManager, appWidgetId);
//            }
        }
    }
}
