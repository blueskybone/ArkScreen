package com.godot17.arksc.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.godot17.arksc.R;

public class PrefManager {

    public enum funcMode {
        FLOAT_WIN,
        TOAST_MSG,
        FLEX,
        UNKNOWN
    }

    public final static float DEFAULT_ALPHA = 1.0f;
    public final static int DEFAULT_WIDTH = 600;
    public final static int DEFAULT_HEIGHT = 800;

    private static SharedPreferences shared;
    private static final String configPath = "arkPreferManager";

    public static SharedPreferences getShared(Context context) {
        try {
            shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shared;
    }

    public static void setShowMode(Context context, PrefManager.funcMode mode) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putInt("funcMode", getMode(mode)).apply();
    }

    public static PrefManager.funcMode getShowMode(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return getMode(shared.getInt("funcMode", getMode(PrefManager.funcMode.FLOAT_WIN)));
    }

    public static int getMode(PrefManager.funcMode mode) {
        if (mode == PrefManager.funcMode.FLOAT_WIN) return 1;
        else if (mode == PrefManager.funcMode.TOAST_MSG) return 2;
        else if (mode == PrefManager.funcMode.FLEX) return 3;
        else return 0;
    }

    public static PrefManager.funcMode getMode(int num) {
        if (num == 1) return PrefManager.funcMode.FLOAT_WIN;
        else if (num == 2) return PrefManager.funcMode.TOAST_MSG;
        else if (num == 3) return PrefManager.funcMode.FLEX;
        else return PrefManager.funcMode.UNKNOWN;
    }

    public static boolean getCheckBoxStatus(Context context, String star) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        boolean status;
        switch (star) {
            case "1":
            case "5":
            case "6":
                status = true;
                break;
            case "2":
            case "3":
            case "4":
            default:
                status = false;
                break;
        }
        return shared.getBoolean("checkBox" + star, status);
    }

    public static void setCheckBoxStatus(Context context, Boolean check, int star) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putBoolean("checkBox" + star, check).apply();
    }

    public static void setCheckBoxStatus(Context context, Boolean check, String star) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putBoolean("checkBox" + star, check).apply();
    }

    public static void setAutoSign(Context context, boolean status) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putBoolean("autoSign", status).apply();
    }

    public static boolean getAutoSign(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getBoolean("autoSign", false);
    }

    /*
     * below should be put into ConfManager.
     * */
    public static void setToken(Context context, String token) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putString("token", token).commit();
    }

    public static String getToken(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getString("token", "");
    }

    public static void setDefaultAccountId(Context context, String token) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putString("DefaultAccountId", token).commit();
    }

    public static String getDefaultAccountId(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getString("token", "");
    }


    public static void setUserId(Context context, String userRoleId) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putString("userRoleId", userRoleId).commit();
    }

    public static String getUserId(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getString("userRoleId", "");
    }

    public static void setCred(Context context, String cred) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putString("cred", cred).commit();
    }

    public static String getCred(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getString("cred", "");
    }

    public static void setCredToken(Context context, String cred) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putString("credToken", cred).commit();
    }

    public static String getCredToken(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getString("credToken", "");
    }

    public static void setChannelMasterId(Context context, String channelMasterId){
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putString("channelMasterId", channelMasterId).commit();
    }
    public static String getChannelMasterId(Context context){
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getString("channelMasterId", "1");
    }

    public static void setUserInfo(Context context, String userInfo) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putString("userInfo", userInfo).commit();
    }

    public static String getUserInfo(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getString("userInfo", "未登录");
    }

    public static int getSignTs(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getInt("signTs", 0);
    }

    public static void setSignTs(Context context, int ts) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putInt("signTs", ts).commit();
    }

    //    public static String getSignItem(Context context){
//        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
//        return shared.getString("signItem","无信息");
//    }
//    public static void setSignItem(Context context, String item){
//        shared = context.getSharedPreferences(configPath,Context.MODE_PRIVATE);
//        shared.edit().putString("signItem",item).apply();
//    }

    public static boolean getTokenChanged(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getBoolean("onTokenChanged", false);
    }

    public static void setTokenChanged(Context context, boolean changed) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putBoolean("onTokenChanged", changed).commit();
    }


    /*
     * Widget Config.
     * */
    public static int getWidgetBgAlpha(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getInt("widgetAlpha", 50);
    }

    public static void setWidgetBgAlpha(Context context, int alpha) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putInt("widgetAlpha", alpha).commit();
    }

    public static int getWidgetBgColor(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getInt("widgetColor", R.color.black);
    }

    public static void setWidgetBgColor(Context context, int colorId) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putInt("widgetColor", colorId).commit();
    }

    public static void setWidgetTextColor(Context context, int colorId) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putInt("widgetTextColor", colorId).commit();
    }

    public static int getWidgetTextColor(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getInt("widgetTextColor", R.color.white);
    }

    public static void setWidgetTextSize(Context context, int size) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putInt("widgetTextSize", size).commit();
    }

    public static int getWidgetTextSize(Context context) {
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getInt("widgetTextSize", 25);
    }


}
