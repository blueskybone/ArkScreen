package com.godot17.arksc;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.godot17.arksc.utils.NetWorkTask.OK;
import static com.godot17.arksc.utils.NetWorkTask.getCredByToken;
import static com.godot17.arksc.utils.PrefManager.getAutoSign;
import static com.godot17.arksc.utils.PrefManager.getToken;
import static com.godot17.arksc.utils.PrefManager.getWidgetBgAlpha;
import static com.godot17.arksc.utils.PrefManager.getWidgetBgColor;
import static com.godot17.arksc.utils.PrefManager.getWidgetTextColor;
import static com.godot17.arksc.utils.PrefManager.getWidgetTextSize;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godot17.arksc.datautils.GameInfo;
import com.godot17.arksc.utils.NetWorkTask;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * Implementation of App Widget functionality.
 */
public class SklandWidget extends AppWidgetProvider {

    private static final String TAG = "SklandWidget";
    public static final String APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String MANUAL_UPDATE = "MANUAL_UPDATE";
    private static final String WORKER_NAME = "SklandWorker";

//    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
//                                int appWidgetId) {
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.skland_widget);
//        int bgColorId = getWidgetBgColor(context);
//        int bgAlpha = getWidgetBgAlpha(context);
//        int textColorId = getWidgetTextColor(context);
//        int textSize = getWidgetTextSize(context);
//        views.setImageViewResource(R.id.widget_bg, R.drawable.widget_background);
//        views.setInt(R.id.widget_bg, "setColorFilter", context.getResources().getColor(bgColorId, null));
//        views.setInt(R.id.widget_bg, "setAlpha", bgAlpha);
//        views.setInt(R.id.appwidget_text, "setTextColor", context.getResources().getColor(textColorId, null));
//        views.setTextViewTextSize(R.id.appwidget_text, COMPLEX_UNIT_SP, textSize);
//        LoadingData(context, views, appWidgetManager, appWidgetId);
//
//        // Instruct the widget manager to update the widget
//        //appWidgetManager.updateAppWidget(appWidgetId, views);
//    }
//
//    private static void LoadingData(Context context, RemoteViews views, AppWidgetManager appWidgetManager,
//                                    int appWidgetId) {
//        new Thread(() -> {
//
//            for (int i = 0; i < 3; i++) {
//                String token = getToken(context);
//                if (token.equals("")) {
//                    views.setTextViewText(R.id.appwidget_text, "未登录");
//                    continue;
//                }
//                try {
//                    InputStream is = getGameInfoInputStream(context);
//                    if (is == null) {
//                        views.setTextViewText(R.id.appwidget_text, "getDataErr");
//                        continue;
//                    }
//                    GZIPInputStream gzip = new GZIPInputStream(is);
//                    ObjectMapper om = new ObjectMapper();
//                    JsonNode dataNode = om.readTree(gzip);
//
//                    if (0 != dataNode.get("code").asInt()) {
//                        gzip.close();
//                        is.close();
//                        String resp = getCredByToken(context);
//                        if (!resp.equals(OK)) {
//                            views.setTextViewText(R.id.appwidget_text, "登录过期");
//                        }
//                        continue;
//                    } else {
//                        getGameInfo(dataNode);
//                        gzip.close();
//                        is.close();
//                    }
//
//                    Log.e(TAG, "理智： " + GameInfo.ap.current + " / " + GameInfo.ap.max);
//                    views.setTextViewText(R.id.appwidget_text, GameInfo.ap.current + " / " + GameInfo.ap.max);
//                    appWidgetManager.updateAppWidget(appWidgetId, views);
//                    if (getAutoSign(context)) {
//                        NetWorkTask.doAttendance(context);
//                    }
//                    break;
//                } catch (Exception e) {
//                    Log.e(TAG, "error with", e);
//                }
//            }
//            appWidgetManager.updateAppWidget(appWidgetId, views);
//        }).start();
//    }
//
//    private static void getGameInfo(JsonNode tree) {
//        int currentTs = tree.at("/data/currentTs").asInt();
//        int ap_current = tree.at("/data/status/ap/current").asInt();
//        int ap_max = tree.at("/data/status/ap/max").asInt();
//        int ap_lastApAddTime = tree.at("/data/status/ap/lastApAddTime").asInt();
//        int ap_recover = tree.at("/data/status/ap/completeRecoveryTime").asInt();
//        GameInfo.ap.max = ap_max;
//        if (ap_recover == -1) {
//            GameInfo.ap.current = ap_current;
//            GameInfo.ap.recoverTime = -1;
//        } else if (ap_recover < currentTs) {
//            GameInfo.ap.current = ap_max;
//            GameInfo.ap.recoverTime = -1;
//        } else {
//            GameInfo.ap.current = (currentTs - ap_lastApAddTime) / (60 * 6) + ap_current;
//            GameInfo.ap.recoverTime = (ap_recover - currentTs);
//        }
//    }

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
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }
        //Retrieve a PendingIntent that will perform a broadcast
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.skland_widget);

        //为刷新按钮绑定一个事件便于发送广播
        remoteViews.setOnClickPendingIntent(R.id.refresh, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(SklandWorker.class,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
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
        if (intent.getAction().equals(MANUAL_UPDATE) ||
                intent.getAction().equals(APPWIDGET_UPDATE)) {
            Log.e(TAG, "receive");
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(SklandWorker.class));

////            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
////            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context.getPackageName(), SklandWidget.class.getName()));
////            for (int appWidgetId : appWidgetIds) {
////                //updateAppWidget(context, appWidgetManager, appWidgetId);
////            }
//        }
        }
    }
}
