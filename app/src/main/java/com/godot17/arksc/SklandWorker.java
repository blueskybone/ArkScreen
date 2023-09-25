package com.godot17.arksc;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.godot17.arksc.utils.NetWorkTask.OK;
import static com.godot17.arksc.utils.NetWorkTask.getCredByToken;
import static com.godot17.arksc.utils.NetWorkTask.getGameInfoInputConnection;
import static com.godot17.arksc.utils.PrefManager.getAutoSign;
import static com.godot17.arksc.utils.PrefManager.getToken;
import static com.godot17.arksc.utils.PrefManager.getWidgetBgAlpha;
import static com.godot17.arksc.utils.PrefManager.getWidgetBgColor;
import static com.godot17.arksc.utils.PrefManager.getWidgetTextColor;
import static com.godot17.arksc.utils.PrefManager.getWidgetTextSize;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godot17.arksc.datautils.GameInfo;
import com.godot17.arksc.utils.NetWorkTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

public class SklandWorker extends Worker {
    private static String TAG = "SklandWorker";

    public SklandWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.skland_widget);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, SklandWidget.class);

        int bgColorId = getWidgetBgColor(context);
        int bgAlpha = getWidgetBgAlpha(context);
        int textColorId = getWidgetTextColor(context);
        int textSize = getWidgetTextSize(context);
        views.setImageViewResource(R.id.widget_bg, R.drawable.widget_background);
        views.setInt(R.id.widget_bg, "setColorFilter", context.getResources().getColor(bgColorId, null));
        views.setInt(R.id.widget_bg, "setAlpha", bgAlpha);
        views.setInt(R.id.appwidget_text, "setTextColor", context.getResources().getColor(textColorId, null));
        views.setTextViewTextSize(R.id.appwidget_text, COMPLEX_UNIT_SP, textSize);


        String token = getToken(context);
        if (token.equals("")) {
            Log.e(TAG, "未登录");
            views.setTextViewText(R.id.appwidget_text, "未登录");
            appWidgetManager.updateAppWidget(componentName, views);
        } else {
            try {
                HttpsURLConnection cn = getGameInfoInputConnection(context);
                InputStream is = cn.getInputStream();
                if (is == null) {
                    Log.e(TAG, "get inputStream fails");
                    views.setTextViewText(R.id.appwidget_text, "getDataErr");
                    appWidgetManager.updateAppWidget(componentName, views);
                    return Result.retry();
                }
                //Log.e("SklandWorker", "get inputStream");

                /*如果只获取理智信息，只需要读到1210kb左右，断开连接。
                * 主要是看jsonNode的readTree的流读入,要整个数据读完才建树。
                * 读一次完整数据要250kb。真不知道yj怎么想的。
                * 更精确的方法是读到需要的信息为止，但挨个判断效率太低*/
                GZIPInputStream gzip = new GZIPInputStream(is);
                InputStreamReader inputStreamReader = new InputStreamReader(gzip);
                char[] chars = new char[1536];

                int s = inputStreamReader.read(chars);
                String str = new String(chars);
                //Log.e(TAG, str);
                String code = getJsonValue(str, "\"code\"", ",");
                //Log.e(TAG, "CODE " + code);
                if (!code.equals("0")) {
                    gzip.close();
                    is.close();
                    cn.disconnect();
                    Log.e(TAG, "disconnect");
                    String resp = getCredByToken(context);
                    if (!resp.equals(OK)) {
                        views.setTextViewText(R.id.appwidget_text, "登录过期");
                        appWidgetManager.updateAppWidget(componentName, views);
                        return Result.failure();
                    } else {
                        return Result.retry();
                    }
                } else {
                    getApInfo(str);
                    gzip.close();
                    is.close();
                    cn.disconnect();
                    Log.e(TAG, "disconnect");
                }

                Log.e("SklandWorker", "理智： " + GameInfo.ap.current + " / " + GameInfo.ap.max);
                views.setTextViewText(R.id.appwidget_text, GameInfo.ap.current + " / " + GameInfo.ap.max);
                appWidgetManager.updateAppWidget(componentName, views);


                /*
                * 如果后面有获取其他实时数据的需求（无人机，训练室）
                * 下面是用JsonNode tree建树获取数据的部分。
                * */
//                ObjectMapper om = new ObjectMapper();
//                JsonNode dataNode = om.readTree(gzip);
//
//                if (0 != dataNode.get("code").asInt()) {
//                    gzip.close();
//                    is.close();
//                    cn.disconnect();
//                    Log.e(TAG, "disconnect");
//                    String resp = getCredByToken(context);
//                    if (!resp.equals(OK)) {
//                        views.setTextViewText(R.id.appwidget_text, "登录过期");
//                    }
//                } else {
//                    getGameInfo(dataNode);
//                    gzip.close();
//                    is.close();
//                    cn.disconnect();
//                    Log.e(TAG, "disconnect");
//                }
//
//                Log.e("SklandWorker", "理智： " + GameInfo.ap.current + " / " + GameInfo.ap.max);
//                views.setTextViewText(R.id.appwidget_text, GameInfo.ap.current + " / " + GameInfo.ap.max);
//                appWidgetManager.updateAppWidget(componentName, views);

                if (getAutoSign(context)) {
                    NetWorkTask.doAttendance(context);
                }
                return Result.success();
            } catch (Exception e) {
                Log.e(TAG, "error with", e);
            }
        }
        return Result.success();
    }

    private void getApInfo(String str) {
        int currentTs = Integer.parseInt(getJsonValue(str, "\"currentTs\"", ","));
        int ap_current = Integer.parseInt(getJsonValue(str, "\"current\"", ","));
        int ap_max = Integer.parseInt(getJsonValue(str, "\"max\"", ","));
        int ap_lastApAddTime = Integer.parseInt(getJsonValue(str, "\"lastApAddTime\"", ","));
        int ap_recover = Integer.parseInt(getJsonValue(str, "\"completeRecoveryTime\"", "}"));
        GameInfo.ap.max = ap_max;
        if (ap_recover == -1) {
            GameInfo.ap.current = ap_current;
            GameInfo.ap.recoverTime = -1;
        } else if (ap_recover < currentTs) {
            GameInfo.ap.current = ap_max;
            GameInfo.ap.recoverTime = -1;
        } else {
            GameInfo.ap.current = (currentTs - ap_lastApAddTime) / (360) + ap_current;
            GameInfo.ap.recoverTime = (ap_recover - currentTs);
        }


    }

    private String getJsonValue(String json, String key, String dot) {
        int begIdx = json.indexOf(key);
        int length = key.length() + 1;
        if (begIdx == -1) {
            return "null";
        } else {
            int endIdx = json.indexOf(dot, begIdx + length);
            return json.substring(begIdx + length, endIdx);
        }
    }


    /*弃用*/
    private static void getGameInfo(JsonNode tree) {
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
            GameInfo.ap.current = (currentTs - ap_lastApAddTime) / (360) + ap_current;
            GameInfo.ap.recoverTime = (ap_recover - currentTs);
        }
    }
}
