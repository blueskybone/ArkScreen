package com.godot17.arksc.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

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
            case "1": case "5": case "6":
                status = true;
                break;
            case "2": case "3": case "4":default:
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

    public static void setToken(Context context, String token){
        shared = context.getSharedPreferences(configPath,Context.MODE_PRIVATE);
        shared.edit().putString("token",token).apply();
    }
    public static String getToken(Context context){
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getString("token","");
    }

    public static void setAutoSign(Context context, boolean status){
        shared = context.getSharedPreferences(configPath,Context.MODE_PRIVATE);
        shared.edit().putBoolean("autoSign",status).apply();
    }
    public static boolean getAutoSign(Context context){
        shared = context.getSharedPreferences(configPath,Context.MODE_PRIVATE);
        return shared.getBoolean("autoSign", false);
    }
    public static void setUserInfo(Context context, String userInfo){
        shared = context.getSharedPreferences(configPath,Context.MODE_PRIVATE);
        shared.edit().putString("userInfo",userInfo).apply();
    }
    public static String getUserInfo(Context context){
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getString("userInfo","未登录状态");
    }
    public static long getSignTs(Context context){
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getLong("signTs",0);
    }
    public static void setSignTs(Context context, long ts){
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        shared.edit().putLong("signTs",ts).apply();
    }

    public static String getSignItem(Context context){
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        return shared.getString("signItem","无信息");
    }
    public static void setSignItem(Context context, String item){
        shared = context.getSharedPreferences(configPath,Context.MODE_PRIVATE);
        shared.edit().putString("signItem",item).apply();
    }

}
