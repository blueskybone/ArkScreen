package com.godot17.arksc;

import static com.godot17.arksc.utils.NetworkUtils.doAttendance;
import static com.godot17.arksc.utils.NetworkUtils.getBindingInfoWith;
import static com.godot17.arksc.utils.NetworkUtils.getCredByGrant;
import static com.godot17.arksc.utils.NetworkUtils.getGameInfoStream;
import static com.godot17.arksc.utils.NetworkUtils.getGrantCodeByToken;
import static com.godot17.arksc.utils.PrefManager.getAutoSign;
import static com.godot17.arksc.utils.PrefManager.getToken;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godot17.arksc.datautils.GameInfo;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.zip.GZIPInputStream;

/**
 * Implementation of App Widget functionality.
 */
public class SklandWidget extends AppWidgetProvider {

    private static String TAG = "SklandWidget";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.skland_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        LoadingData(context, views,appWidgetManager,appWidgetId);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void LoadingData(Context context, RemoteViews views, AppWidgetManager appWidgetManager,
                                    int appWidgetId) {
        new Thread(() -> {
            String token = getToken(context);
            if (token.equals("")) {
                views.setTextViewText(R.id.appwidget_text, "未登录");
                appWidgetManager.updateAppWidget(appWidgetId, views);
                return;
            }
            try {
                String code = getGrantCodeByToken(token);
                if (code == null) {
                    views.setTextViewText(R.id.appwidget_text, "未登录");
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    return;
                } else if (code.equals("UNAUTHORIZED")) {
                    views.setTextViewText(R.id.appwidget_text, "登录过期");
                    appWidgetManager.updateAppWidget(appWidgetId, views);

                    return;
                }

                //get cred
                String cred = getCredByGrant(code);
                if (cred == null) {
                    views.setTextViewText(R.id.appwidget_text, "登录失败");
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    return;
                }

                //get uid and gameId
                String uid = getBindingInfoWith(cred, "uid");
                String channelMasterId = getBindingInfoWith(cred, "channelMasterId");
                if (uid == null) {
                    views.setTextViewText(R.id.appwidget_text, "登录失败");
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    return;
                }

                //getInfo
                InputStream is = getGameInfoStream(cred, uid);

                if (is == null) {
                    return;
                }
                GZIPInputStream gzip = new GZIPInputStream(is);
                ObjectMapper om = new ObjectMapper();
                JsonNode tree = om.readTree(gzip);

                int currentTs = tree.at("/data/currentTs").asInt();
                int ap_current = tree.at("/data/status/ap/current").asInt();
                int ap_max = tree.at("/data/status/ap/max").asInt();
                int ap_lastApAddTime = tree.at("/data/status/ap/lastApAddTime").asInt();
                int ap_recover = tree.at("/data/status/ap/completeRecoveryTime").asInt();
                GameInfo.ap.max = ap_max;
                if (ap_recover == -1) {
                    GameInfo.ap.current = ap_current;
                    GameInfo.ap.recoverTime = -1;
                } else if (ap_recover < currentTs) {
                    GameInfo.ap.current = ap_max;
                    GameInfo.ap.recoverTime = -1;
                } else {
                    GameInfo.ap.current = (currentTs - ap_lastApAddTime) / (60 * 6) + ap_current;
                    GameInfo.ap.recoverTime = (ap_recover - currentTs);
                }
                Log.e(TAG, "理智： " + GameInfo.ap.current + " / " + GameInfo.ap.max);
                views.setTextViewText(R.id.appwidget_text, GameInfo.ap.current + " / " + GameInfo.ap.max);
                appWidgetManager.updateAppWidget(appWidgetId, views);
                if (getAutoSign(context)) {
                    if (channelMasterId == null) {
                        return;
                    }
                    doAutoSigh(cred, uid, channelMasterId);
                }
            } catch (Exception e) {
                Log.e(TAG, "error with", e);
            }


        }).start();
    }

    private static void doAutoSigh(String cred, String uid, String channelMasterId) throws MalformedURLException, MalformedURLException {
        String resp = doAttendance(cred, uid, channelMasterId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}