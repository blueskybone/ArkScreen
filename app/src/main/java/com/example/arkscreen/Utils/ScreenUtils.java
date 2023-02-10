package com.example.arkscreen.Utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtils {

    public static DisplayMetrics getMetrics(Context context){
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display mDisplay = window.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        mDisplay.getRealMetrics(metrics);
        return metrics;
    }

    public static int getRealWidth(Context context){
        DisplayMetrics metrics = getMetrics(context);
        return metrics.widthPixels;
    }
    public static int getRealHeight(Context context){
        DisplayMetrics metrics = getMetrics(context);
        return metrics.heightPixels;
    }
    public static int getDensityDpi(Context context){
        DisplayMetrics metrics = getMetrics(context);
        return metrics.densityDpi;
    }

}
