package com.example.arkscreen.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.arkscreen.R;

import java.util.Map;
import java.util.Set;

import io.noties.markwon.Markwon;

// 全局变量
public class ConfigUtils {

    public final static int THEME_KALT = 1000;
    public final static int THEME_AMIYA = 1001;
    public final static int THEME_ROSMO = 1002;
    public final static int THEME_SKADI = 1003;
    public final static int THEME_PTILO = 1004;
    public final static int SHOW_MODE_DEFAULT = 2000;
    public final static int SHOW_MODE_SIMPLE = 2001;
    public final static int DEFAULT_WIDTH = 800;
    public final static int DEFAULT_HEIGHT = 800;
    public final static int DEFAULT_TEXT_SIZE = 16;
    public final static float DEFAULT_ALPHA = 1.0F;

    public static int SHOW_MODE = SHOW_MODE_DEFAULT;
    public static int USER_THEME = THEME_KALT;
    public static int FLOAT_WIDTH = DEFAULT_WIDTH;
    public static int FLOAT_HEIGHT = DEFAULT_HEIGHT;
    public static int TEXT_SIZE = DEFAULT_TEXT_SIZE;
    public static float FLOAT_ALPHA = DEFAULT_ALPHA;

    private static SharedPreferences shared;
    private static final String configPath = "appSetting";

    public static SharedPreferences getShared(Context context) {
        try {
            shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shared;
    }
    @SuppressLint("ApplySharedPref")
    public static void setProperTiesWidth(Context context, int width){
        FLOAT_WIDTH = width;
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt("floatWindowWidth",width);
        editor.commit();

    }
    @SuppressLint("ApplySharedPref")
    public static void setProperTiesHeight(Context context,int height){
        FLOAT_HEIGHT = height;
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt("floatWindowHeight",height);
        editor.commit();
    }
    @SuppressLint("ApplySharedPref")
    public static void setProperTiesAlpha(Context context,float alpha){
        FLOAT_ALPHA = alpha;
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putFloat("floatWindowAlpha",alpha);
        editor.commit();
    }
    @SuppressLint("ApplySharedPref")
    public static void setProperTiesTheme(Context context, int theme){
        USER_THEME = theme;
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt("theme",theme);
        editor.commit();
    }
    @SuppressLint("ApplySharedPref")
    public static void setProperTiesMode(Context context, int showMode){
        SHOW_MODE = showMode;
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt("showMode",showMode);
        editor.commit();
    }
    @SuppressLint("ApplySharedPref")
    public static void setProperTiesTextSize(Context context ,int textSize){
        TEXT_SIZE = textSize;
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt("textSize",textSize);
        editor.commit();
    }

    public static void initialProperTies(Context context){
        shared = context.getSharedPreferences(configPath, Context.MODE_PRIVATE);
        FLOAT_WIDTH = shared.getInt("floatWindowWidth",DEFAULT_WIDTH);
        FLOAT_HEIGHT = shared.getInt("floatWindowHeight",DEFAULT_HEIGHT);
        FLOAT_ALPHA = shared.getFloat("floatWindowAlpha",DEFAULT_ALPHA);
        USER_THEME = shared.getInt("theme",THEME_KALT);
        SHOW_MODE= shared.getInt("showMode",SHOW_MODE_DEFAULT);
        TEXT_SIZE = shared.getInt("textSize",DEFAULT_TEXT_SIZE);
    }
    public static void resetConfig(Context context){
        shared = context.getSharedPreferences(configPath,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt("floatWindowWidth",DEFAULT_WIDTH);
        editor.putInt("floatWindowHeight",DEFAULT_HEIGHT);
        editor.putFloat("floatWindowAlpha",DEFAULT_ALPHA);
        editor.putInt("theme",THEME_KALT);
        editor.putInt("showMode",SHOW_MODE_DEFAULT);
        editor.putInt("textSize",DEFAULT_TEXT_SIZE);
        editor.apply();
    }
}
